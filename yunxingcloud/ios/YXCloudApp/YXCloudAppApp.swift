import SwiftUI

@main
struct YXCloudAppApp: App {
    @State private var auth = AuthStore()
    @State private var cart = CartStore()
    @State private var home = HomeStore()
    @State private var catalog = CatalogStore()

    var body: some Scene {
        WindowGroup {
            RootTabView()
                .environment(auth)
                .environment(cart)
                .environment(home)
                .environment(catalog)
        }
    }
}
