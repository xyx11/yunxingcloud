import Foundation
import Combine

@MainActor
final class HomeStore: ObservableObject {
    @Published var data: HomeData?
    @Published var isLoading = false
    @Published var errorMessage: String?

    private let client = APIClient.shared

    func load(force: Bool = false) async {
        if data != nil && !force { return }
        isLoading = true
        defer { isLoading = false }
        do {
            data = try await client.request(Endpoints.home)
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
