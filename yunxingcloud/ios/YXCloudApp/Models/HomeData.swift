import Foundation

/// GET /api/aggregate/home 响应
struct HomeData: Codable {
    let hotProducts: [Product]
    let newProducts: [Product]
    let categories: [Category]
    let banners: [Banner]

    var usableBanners: [Banner] { banners.filter(\.isUsable) }
    var topLevelCategories: [Category] { categories.filter(\.isTopLevel) }
}
