import Foundation

/// 订单（宽容解码，后端 OrderHead 实体）
struct OrderHead: Codable, Hashable {
    let id: Int?
    let orderNo: String?
    let status: String?
    let totalAmount: Int?
    let payAmount: Int?
    let freight: Int?
    let couponAmount: Int?
    let receiverName: String?
    let receiverPhone: String?
    let receiverAddress: String?
    let createTime: String?
    let username: String?
}
