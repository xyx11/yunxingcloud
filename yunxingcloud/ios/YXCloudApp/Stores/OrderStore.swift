import Foundation
import Combine

@MainActor
final class OrderStore: ObservableObject {
    @Published var orders: [OrderHead] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var toastMessage: String?

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

    /// 发起支付（channel: wechat / alipay），成功后订单状态变已付款
    func pay(order: OrderHead, channel: String) async throws {
        struct PayBody: Encodable { let channel: String }
        guard let id = order.id else { throw AppError.network("无效的订单") }
        let _: EmptyResponse = try await client.request(
            Endpoints.payOrder(id), method: "POST",
            body: PayBody(channel: channel),
            auth: true
        )
        toastMessage = "支付成功"
        await load()
    }
}
