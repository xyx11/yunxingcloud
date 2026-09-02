import SwiftUI

struct RootTabView: View {
    @EnvironmentObject private var cart: CartStore

    var body: some View {
        TabView {
            HomeView()
                .tabItem { Label("首页", systemImage: "house.fill") }

            CategoryView()
                .tabItem { Label("分类", systemImage: "square.grid.2x2.fill") }

            CartView()
                .tabItem { Label("购物车", systemImage: "cart.fill") }
                .badge(cart.totalQuantity)

            ProfileView()
                .tabItem { Label("我的", systemImage: "person.fill") }
        }
        .tint(AppConfig.brandRed)
    }
}
