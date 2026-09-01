import Foundation

/// 分页结果。后端存在三种形态，解码时逐一兜底：
/// 1. Spring Page: { content: [...], totalElements, totalPages, number, size }
/// 2. 自定义: { list: [...], total, page, pageSize }
/// 3. 裸数组: [...]
struct PageResult<T: Decodable>: Decodable {
    let items: [T]
    let totalElements: Int
    let totalPages: Int
    let page: Int

    enum Keys: String, CodingKey {
        case content, list, totalElements, total, totalPages, number, page
    }

    init(from decoder: Decoder) throws {
        if let arr = try? decoder.singleValueContainer().decode([T].self) {
            items = arr
            totalElements = arr.count
            totalPages = 1
            page = 0
            return
        }
        let c = try decoder.container(keyedBy: Keys.self)
        items = try c.decodeIfPresent([T].self, forKey: .content)
            ?? c.decodeIfPresent([T].self, forKey: .list)
            ?? []
        totalElements = try c.decodeIfPresent(Int.self, forKey: .totalElements)
            ?? c.decodeIfPresent(Int.self, forKey: .total)
            ?? items.count
        totalPages = try c.decodeIfPresent(Int.self, forKey: .totalPages) ?? 1
        page = try c.decodeIfPresent(Int.self, forKey: .number)
            ?? c.decodeIfPresent(Int.self, forKey: .page)
            ?? 0
    }

    var hasMore: Bool { page + 1 < totalPages }
}
