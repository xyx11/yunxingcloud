import Foundation

struct LoginRequest: Codable {
    let username: String
    let password: String
}

struct RegisterRequest: Codable {
    let username: String
    let password: String
    let email: String?
}

/// POST /api/login 响应
struct LoginResponse: Codable {
    let success: Bool?
    let username: String?
    let accessToken: String?
    let refreshToken: String?
    let tokenType: String?
    let expiresIn: Int?
    let message: String?
}

/// 错误响应体 { success: false, message: "..." }
struct ApiErrorBody: Codable {
    let success: Bool?
    let message: String?
}
