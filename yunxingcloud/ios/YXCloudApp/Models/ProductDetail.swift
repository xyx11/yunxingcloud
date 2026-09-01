import Foundation

/// GET /api/products/{id}/detail 聚合响应
struct ProductDetail: Codable {
    let product: Product
    let related: [Product]?
    let reviews: [Review]?

    var images: [String] {
        var list = product.images ?? []
        if let main = product.imageUrl, !main.isEmpty, !list.contains(main) {
            list.insert(main, at: 0)
        }
        return list
    }
}

struct Review: Codable, Hashable {
    let id: Int?
    let rating: Int?
    let content: String?
    let username: String?
    let createdAt: String?
}
