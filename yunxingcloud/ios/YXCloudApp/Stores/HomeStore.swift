import Foundation
import Observation

@MainActor
@Observable
final class HomeStore {
    var data: HomeData?
    var isLoading = false
    var errorMessage: String?

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
