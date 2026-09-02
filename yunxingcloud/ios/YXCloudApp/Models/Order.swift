import Foundation

/// 支付发起响应（含当面付二维码）
struct PayResponse: Codable {
    let channel: String?
    let orderNo: String?
    let qrCode: String?
    let tradeNo: String?
    let mock: Bool?
    let paymentOrderId: Int?

    var isQrPay: Bool { !(qrCode ?? "").isEmpty }
}

/// 订单（宽容解码，后端 OrderHead 实体）
struct OrderHead: Codable, Hashable {
    let id: Int?
    let orderNo: String?
    let status: String?
    let totalAmount: Int?
    let actualAmount: Int?
    let couponAmount: Int?
    let receiverName: String?
    let receiverPhone: String?
    let receiverAddress: String?
    let remark: String?
    let createdAt: String?
    let username: String?

    /// 状态中文描述：0待付款/1已付款/2已发货/3已完成/4已取消
    var statusText: String {
        switch status ?? "" {
        case "0": return "待付款"
        case "1": return "已付款"
        case "2": return "已发货"
        case "3": return "已完成"
        case "4": return "已取消"
        default: return status ?? "未知"
        }
    }
}
