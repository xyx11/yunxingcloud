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

    /// 发起支付（channel: wechat / alipay），返回支付响应（当面付含收款二维码）
    @discardableResult
    func pay(order: OrderHead, channel: String) async throws -> PayResponse {
        struct PayBody: Encodable { let channel: String }
        guard let id = order.id else { throw AppError.network("无效的订单") }
        let resp: PayResponse = try await client.request(
            Endpoints.payOrder(id), method: "POST",
            body: PayBody(channel: channel),
            auth: true
        )
        // 非二维码支付（模拟/即时）直接刷新；二维码支付等回调后由轮询刷新
        if !resp.isQrPay {
            toastMessage = "支付成功"
            await load()
        }
        return resp
    }

    /// 查询单笔订单最新状态（二维码支付轮询用）
    func refreshOrder(id: Int) async -> OrderHead? {
        guard let page: PageResult<OrderHead> = try? await client.request(
            Endpoints.orders, query: ["page": "0", "size": "50"], auth: true
        ) else { return nil }
        return page.items.first { $0.id == id }
    }
}
