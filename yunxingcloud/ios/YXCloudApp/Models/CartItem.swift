import Foundation

/// 后端 CartItem 实体：id/username/productId/productName/price(分)/quantity/imageUrl/createdAt
struct CartItem: Codable, Identifiable, Hashable {
    var id: Int
    var productId: Int?
    var productName: String?
    var price: Int?
    var quantity: Int
    var imageUrl: String?
    var username: String?

    var priceFen: Int { price ?? 0 }
}

/// GET /api/cart 响应：{ items: [...], recommended: [...] }
struct CartListResponse: Codable {
    let items: [CartItem]
    let recommended: [Product]?
}
