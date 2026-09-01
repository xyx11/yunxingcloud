import Foundation

struct Banner: Codable, Identifiable, Hashable {
    let id: Int
    let title: String?
    let imageUrl: String?
    let linkUrl: String?
    let sortOrder: Int?
    let status: String?

    /// 空图 banner 不展示（数据源里存在 imageUrl 为空的记录）
    var isUsable: Bool { !(imageUrl ?? "").isEmpty }
}
