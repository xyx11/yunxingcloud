import Foundation
import Combine

@MainActor
final class AuthStore: ObservableObject {
    @Published var isLoggedIn = false
    @Published var username: String?
    @Published var currentUser: User?
    @Published var isBusy = false
    @Published var errorMessage: String?

    private let client = APIClient.shared

    init() {
        // 启动时从 Keychain 恢复会话
        if let token = client.accessToken, !token.isEmpty {
            isLoggedIn = true
            username = KeychainStore.read(APIClient.TokenKey.username)
            Task { await loadUser() }
        }
        client.onSessionExpired = { [weak self] in
            Task { @MainActor in self?.forceLogout() }
        }
    }

    func login(username: String, password: String) async throws {
        isBusy = true
        defer { isBusy = false }
        let resp: LoginResponse = try await client.request(
            Endpoints.login, method: "POST",
            body: LoginRequest(username: username, password: password)
        )
        guard let access = resp.accessToken, !access.isEmpty else {
            throw AppError.http(status: 401, message: resp.message ?? "登录失败")
        }
        client.saveTokens(access: access, refresh: resp.refreshToken ?? "")
        KeychainStore.save(username, for: APIClient.TokenKey.username)
        self.username = resp.username ?? username
        isLoggedIn = true
        errorMessage = nil
        await loadUser()
    }

    func register(username: String, password: String, email: String?) async throws {
        isBusy = true
        defer { isBusy = false }
        let _: LoginResponse = try await client.request(
            Endpoints.register, method: "POST",
            body: RegisterRequest(username: username, password: password, email: email)
        )
    }

    func logout() {
        client.clearTokens()
        isLoggedIn = false
        username = nil
        currentUser = nil
    }

    /// refresh 失败时由 APIClient 回调触发
    func forceLogout() {
        client.clearTokens()
        isLoggedIn = false
        username = nil
        currentUser = nil
        errorMessage = "登录已过期，请重新登录"
    }

    func loadUser() async {
        guard isLoggedIn else { return }
        do {
            currentUser = try await client.request(Endpoints.user, auth: true)
        } catch {
            // 用户信息加载失败不阻塞浏览
        }
    }
}
