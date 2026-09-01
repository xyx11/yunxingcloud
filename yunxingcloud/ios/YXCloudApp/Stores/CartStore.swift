import Foundation
import Observation

@MainActor
@Observable
final class CartStore {
    var items: [CartItem] = []
    var isLoading = false
    var errorMessage: String?
    var toastMessage: String?

    private let client = APIClient.shared

    var totalQuantity: Int { items.reduce(0) { $0 + $1.quantity } }

    /// 合计金额（分）
    var totalPriceFen: Int { items.reduce(0) { $0 + $1.priceFen * $1.quantity } }
    var totalPriceYuan: Decimal { Decimal(totalPriceFen) / 100 }

    func load() async {
        guard client.accessToken != nil else {
            items = []
            return
        }
        isLoading = true
        defer { isLoading = false }
        do {
            let resp: CartListResponse = try await client.request(Endpoints.cart, auth: true)
            items = resp.items
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    /// 加购成功返回 true，未登录返回 false（由视图层引导登录）
    func add(productId: Int, quantity: Int = 1) async -> Bool {
        guard client.accessToken != nil else { return false }
        struct AddBody: Encodable { let productId: Int; let quantity: Int }
        do {
            let _: CartItem = try await client.request(
                Endpoints.cart, method: "POST",
                body: AddBody(productId: productId, quantity: quantity),
                auth: true
            )
            toastMessage = "已加入购物车"
            await load()
            return true
        } catch {
            toastMessage = error.localizedDescription
            return true // 已登录但失败：不让视图误以为未登录
        }
    }

    func updateQuantity(item: CartItem, quantity: Int) async {
        struct UpdateBody: Encodable { let quantity: Int }
        do {
            let updated: CartItem = try await client.request(
                Endpoints.cartItem(item.id), method: "PUT",
                body: UpdateBody(quantity: quantity),
                auth: true
            )
            if let idx = items.firstIndex(where: { $0.id == item.id }) {
                items[idx] = updated
            }
        } catch {
            toastMessage = error.localizedDescription
        }
    }

    func remove(item: CartItem) async {
        do {
            try await client.requestVoid(Endpoints.cartItem(item.id), method: "DELETE", auth: true)
            items.removeAll { $0.id == item.id }
        } catch {
            toastMessage = error.localizedDescription
        }
    }
}
