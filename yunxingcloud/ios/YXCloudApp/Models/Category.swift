import Foundation

struct Category: Codable, Identifiable, Hashable {
    let id: Int
    let name: String
    let icon: String?
    let parentId: Int?
    let sortOrder: Int?
    let status: String?

    var isTopLevel: Bool { (parentId ?? 0) == 0 }
}
