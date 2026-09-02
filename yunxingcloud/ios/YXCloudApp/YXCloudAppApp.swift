import SwiftUI

@main
@MainActor
struct YXCloudAppApp: App {
    @StateObject private var auth = AuthStore()
    @StateObject private var cart = CartStore()
    @StateObject private var home = HomeStore()
    @StateObject private var catalog = CatalogStore()
    @StateObject private var orderStore = OrderStore()
    @StateObject private var addressStore = AddressStore()

    var body: some Scene {
        WindowGroup {
            RootTabView()
                .environmentObject(auth)
                .environmentObject(cart)
                .environmentObject(home)
                .environmentObject(catalog)
                .environmentObject(orderStore)
                .environmentObject(addressStore)
        }
    }
}
