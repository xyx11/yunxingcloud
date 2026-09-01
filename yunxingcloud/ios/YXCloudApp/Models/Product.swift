import Foundation

struct Product: Codable, Identifiable, Hashable {
    let id: Int
    let name: String
    let price: Int
    let originalPrice: Int?
    let imageUrl: String?
    let images: [String]?
    let description: String?
    let sales: Int?
    let stock: Int?
    let isHot: Bool?
    let isNew: Bool?
    let categoryId: Int?
    let brandId: Int?
    let tags: String?

    enum CodingKeys: String, CodingKey {
        case id, name, price, originalPrice, imageUrl, images, description
        case sales, stock, isHot, isNew, categoryId, brandId, tags
    }

    /// 价格单位是分，转换为元显示
    var priceYuan: Decimal { Decimal(price) / 100 }

    /// 商品主图：优先 imageUrl，其次 images 第一张
    var mainImage: String? {
        if let imageUrl, !imageUrl.isEmpty { return imageUrl }
        return images?.first
    }
}
