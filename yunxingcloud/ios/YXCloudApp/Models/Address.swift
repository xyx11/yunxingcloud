import Foundation

/// 收货地址（后端 UserAddress 实体）
struct UserAddress: Codable, Identifiable, Hashable {
    var id: Int?
    var name: String
    var phone: String
    var province: String?
    var city: String?
    var district: String?
    var detail: String?
    var isDefault: Bool?

    var fullRegion: String {
        let p = province ?? ""
        let c = city ?? ""
        let d = district ?? ""
        return [p, c, d].filter { !$0.isEmpty }.joined(separator: " ")
    }

    var fullAddress: String {
        let detailText = detail ?? ""
        return detailText.isEmpty ? fullRegion : "\(fullRegion) \(detailText)"
    }
}

/// 新增/编辑地址请求体（username 由后端从 token 获取）
struct AddressRequest: Codable {
    var name: String
    var phone: String
    var province: String?
    var city: String?
    var district: String?
    var detail: String?
    var isDefault: Bool?
}
