import SwiftUI

struct RootTabView: View {
    @EnvironmentObject private var cart: CartStore
    @EnvironmentObject private var auth: AuthStore
    @EnvironmentObject private var orderStore: OrderStore
    @EnvironmentObject private var addressStore: AddressStore

    @State private var toastText: String?
    @State private var toastTask: Task<Void, Never>?

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
        .overlay(alignment: .bottom) {
            if let toastText {
                ToastView(message: toastText)
                    .animation(.spring(duration: 0.3), value: toastText)
            }
        }
        .onChange(of: cart.toastMessage) { msg in showToast(msg) }
        .onChange(of: auth.toastMessage) { msg in showToast(msg) }
        .onChange(of: orderStore.toastMessage) { msg in showToast(msg) }
        .onChange(of: addressStore.toastMessage) { msg in showToast(msg) }
    }

    private func showToast(_ message: String?) {
        guard let message, !message.isEmpty else { return }
        toastTask?.cancel()
        toastText = message
        toastTask = Task {
            try? await Task.sleep(for: .seconds(2.2))
            if !Task.isCancelled { toastText = nil }
        }
    }
}
