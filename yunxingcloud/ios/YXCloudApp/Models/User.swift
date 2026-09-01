import Foundation

/// GET /api/user 响应（字段宽容解码，后端可能只返回部分字段）
struct User: Codable, Hashable {
    let id: Int?
    let username: String?
    let nickname: String?
    let email: String?
    let avatar: String?
    let phone: String?

    var displayName: String { nickname ?? username ?? "用户" }
}
