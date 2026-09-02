import Foundation

enum Endpoints {
    // 首页聚合
    static let home = "/api/aggregate/home"
    // 商品
    static let products = "/api/products"
    static func productDetail(_ id: Int) -> String { "/api/products/\(id)/detail" }
    static let search = "/api/products/search"
    static let categories = "/api/categories"
    // 购物车
    static let cart = "/api/cart"
    static func cartItem(_ id: Int) -> String { "/api/cart/\(id)" }
    // 订单
    static let orders = "/api/orders"
    static func payOrder(_ id: Int) -> String { "/api/orders/\(id)/pay" }
    // 收货地址
    static let addresses = "/api/addresses"
    static func address(_ id: Int) -> String { "/api/addresses/\(id)" }
    // 认证
    static let login = "/api/login"
    static let refresh = "/api/refresh"
    static let register = "/api/register"
    static let user = "/api/user"
}
