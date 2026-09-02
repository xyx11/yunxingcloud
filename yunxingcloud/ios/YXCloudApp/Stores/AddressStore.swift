import Foundation
import Combine

@MainActor
final class AddressStore: ObservableObject {
    @Published var addresses: [UserAddress] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var toastMessage: String?

    private let client = APIClient.shared

    func load() async {
        guard client.accessToken != nil else {
            addresses = []
            return
        }
        isLoading = true
        defer { isLoading = false }
        do {
            addresses = try await client.request(Endpoints.addresses, auth: true)
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func add(_ request: AddressRequest) async throws {
        let _: UserAddress = try await client.request(
            Endpoints.addresses, method: "POST", body: request, auth: true
        )
        toastMessage = "地址已添加"
        await load()
    }

    func update(id: Int, _ request: AddressRequest) async throws {
        let _: UserAddress = try await client.request(
            Endpoints.address(id), method: "PUT", body: request, auth: true
        )
        toastMessage = "地址已更新"
        await load()
    }

    func delete(_ address: UserAddress) async {
        guard let id = address.id else { return }
        do {
            try await client.requestVoid(Endpoints.address(id), method: "DELETE", auth: true)
            toastMessage = "地址已删除"
            await load()
        } catch {
            toastMessage = error.localizedDescription
        }
    }

    var defaultAddress: UserAddress? {
        addresses.first { $0.isDefault == true } ?? addresses.first
    }
}
