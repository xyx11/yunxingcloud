import Foundation
import Combine

@MainActor
final class OrderStore: ObservableObject {
    @Published var orders: [OrderHead] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    private let client = APIClient.shared

    func load() async {
        guard client.accessToken != nil else {
            orders = []
            return
        }
        isLoading = true
        defer { isLoading = false }
        do {
            let page: PageResult<OrderHead> = try await client.request(
                Endpoints.orders,
                query: ["page": "0", "size": "50"],
                auth: true
            )
            orders = page.items
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
