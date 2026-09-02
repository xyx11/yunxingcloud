import SwiftUI

@main
@MainActor
struct YXCloudAppApp: App {
    @StateObject private var auth = AuthStore()
    @StateObject private var cart = CartStore()
    @StateObject private var home = HomeStore()
    @StateObject private var catalog = CatalogStore()

    var body: some Scene {
        WindowGroup {
            RootTabView()
                .environmentObject(auth)
                .environmentObject(cart)
                .environmentObject(home)
                .environmentObject(catalog)
        }
    }
}
