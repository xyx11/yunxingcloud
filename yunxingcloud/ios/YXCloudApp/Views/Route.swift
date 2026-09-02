import Foundation

/// 导航路由（配合 NavigationPath，规避 iOS 16 早期版本 LazyVGrid 内 NavigationLink 不响应的 bug）
enum Route: Hashable {
    case products(title: String, categoryId: Int?)
    case productDetail(Int)
    case search
    case login
    case register
}
