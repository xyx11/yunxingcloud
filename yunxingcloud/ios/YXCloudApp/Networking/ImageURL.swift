import Foundation

/// 图片地址解析：后端可能返回绝对 URL（picsum/alicdn）或相对路径（/uploads/...）
enum ImageURL {
    static func resolve(_ raw: String?) -> URL? {
        guard let raw, !raw.isEmpty else { return nil }
        if raw.hasPrefix("http://") || raw.hasPrefix("https://") {
            return URL(string: raw)
        }
        return URL(string: raw, relativeTo: AppConfig.baseURL)?.absoluteURL
    }
}
