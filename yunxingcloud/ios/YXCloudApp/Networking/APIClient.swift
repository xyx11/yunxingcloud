import Foundation

enum AppError: LocalizedError {
    case network(String)
    case http(status: Int, message: String)
    case decoding
    case unauthorized

    var errorDescription: String? {
        switch self {
        case .network(let msg): return msg
        case .http(_, let message): return message
        case .decoding: return "数据解析失败"
        case .unauthorized: return "登录已过期，请重新登录"
        }
    }
}

/// 401 时并发请求共享同一次 refresh（单飞）
actor RefreshCoordinator {
    private var inFlight: Task<String?, Never>?

    func refresh(using client: APIClient) async -> String? {
        if let inFlight { return await inFlight.value }
        let task = Task<String?, Never> { await client.performRefresh() }
        inFlight = task
        let token = await task.value
        inFlight = nil
        return token
    }
}

final class APIClient {
    static let shared = APIClient()

    private let session: URLSession
    private let refresher = RefreshCoordinator()

    /// 会话过期（refresh 失败）时通知 AuthStore 登出
    var onSessionExpired: (() -> Void)?

    private init() {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 15
        config.timeoutIntervalForResource = 30
        config.requestCachePolicy = .reloadRevalidatingCacheData
        config.urlCache = URLCache(memoryCapacity: 20 * 1024 * 1024, diskCapacity: 100 * 1024 * 1024)
        session = URLSession(configuration: config)
    }

    // MARK: - Token 存取（Keychain 为单一数据源）

    enum TokenKey {
        static let access = "accessToken"
        static let refresh = "refreshToken"
        static let username = "username"
    }

    var accessToken: String? { KeychainStore.read(TokenKey.access) }
    var refreshToken: String? { KeychainStore.read(TokenKey.refresh) }

    func saveTokens(access: String, refresh: String) {
        KeychainStore.save(access, for: TokenKey.access)
        KeychainStore.save(refresh, for: TokenKey.refresh)
    }

    func clearTokens() {
        KeychainStore.delete(TokenKey.access)
        KeychainStore.delete(TokenKey.refresh)
        KeychainStore.delete(TokenKey.username)
    }

    // MARK: - 通用请求

    func request<T: Decodable>(
        _ path: String,
        method: String = "GET",
        body: Encodable? = nil,
        query: [String: String] = [:],
        auth: Bool = false
    ) async throws -> T {
        try await send(path: path, method: method, body: body, query: query, auth: auth)
    }

    /// 无响应体的请求（DELETE 等）
    func requestVoid(
        _ path: String,
        method: String = "GET",
        body: Encodable? = nil,
        auth: Bool = false
    ) async throws {
        let _: EmptyResponse = try await send(path: path, method: method, body: body, query: [:], auth: auth)
    }

    // MARK: - 内部实现

    private func send<T: Decodable>(
        path: String,
        method: String,
        body: Encodable?,
        query: [String: String],
        auth: Bool
    ) async throws -> T {
        do {
            return try await perform(path: path, method: method, body: body, query: query, auth: auth)
        } catch AppError.http(let status, _) where status == 401 && auth {
            // 尝试用 refreshToken 换新 token 后重试一次
            guard let newToken = await refresher.refresh(using: self) else {
                onSessionExpired?()
                throw AppError.unauthorized
            }
            if newToken.isEmpty {
                onSessionExpired?()
                throw AppError.unauthorized
            }
            return try await perform(path: path, method: method, body: body, query: query, auth: auth)
        }
    }

    private func perform<T: Decodable>(
        path: String,
        method: String,
        body: Encodable?,
        query: [String: String],
        auth: Bool
    ) async throws -> T {
        var components = URLComponents(url: AppConfig.baseURL.appendingPathComponent(path), resolvingAgainstBaseURL: false)
        if !query.isEmpty {
            components?.queryItems = query.map { URLQueryItem(name: $0.key, value: $0.value) }
        }
        guard let url = components?.url else { throw AppError.network("无效的请求地址") }

        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if auth, let token = accessToken {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        if let body {
            request.httpBody = try JSONEncoder().encode(body)
        }

        let data: Data
        let response: URLResponse
        do {
            (data, response) = try await session.data(for: request)
        } catch {
            throw AppError.network("网络连接失败，请检查网络")
        }

        guard let http = response as? HTTPURLResponse else { throw AppError.network("无效的服务器响应") }

        guard (200..<300).contains(http.statusCode) else {
            let message = (try? JSONDecoder().decode(ApiErrorBody.self, from: data))?.message
            throw AppError.http(status: http.statusCode, message: message ?? "请求失败（\(http.statusCode)）")
        }

        if data.isEmpty {
            if let empty = EmptyResponse() as? T { return empty }
            throw AppError.decoding
        }
        do {
            return try JSONDecoder().decode(T.self, from: data)
        } catch {
            throw AppError.decoding
        }
    }

    /// 执行 refresh（在 RefreshCoordinator 单飞保护下调用）
    fileprivate func performRefresh() async -> String? {
        guard let refreshToken else { return nil }
        struct RefreshBody: Encodable { let refreshToken: String }
        struct RefreshResponse: Decodable { let accessToken: String?; let refreshToken: String? }
        do {
            let resp: RefreshResponse = try await perform(
                path: Endpoints.refresh, method: "POST",
                body: RefreshBody(refreshToken: refreshToken),
                query: [:], auth: false
            )
            if let newAccess = resp.accessToken {
                saveTokens(access: newAccess, refresh: resp.refreshToken ?? refreshToken)
                return newAccess
            }
            return nil
        } catch {
            return nil
        }
    }
}

struct EmptyResponse: Decodable {}
