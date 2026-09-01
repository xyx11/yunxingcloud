import Foundation
import Observation

@MainActor
@Observable
final class CatalogStore {
    var categories: [Category] = []
    var products: [Product] = []
    var isLoading = false
    var isLoadingMore = false
    var errorMessage: String?

    private var currentPage = 0
    private var totalPages = 1
    private var currentCategoryId: Int?
    private var currentKeyword = ""

    var topLevelCategories: [Category] { categories.filter(\.isTopLevel) }

    private let client = APIClient.shared

    func loadCategories() async {
        do {
            categories = try await client.request(Endpoints.categories)
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    /// 按分类加载商品（第一页）
    func loadProducts(categoryId: Int?) async {
        currentCategoryId = categoryId
        currentKeyword = ""
        currentPage = 0
        isLoading = true
        defer { isLoading = false }
        do {
            var query = ["page": "\(currentPage)", "size": "\(AppConfig.pageSize)", "sort": "sales,desc"]
            if let categoryId { query["categoryId"] = "\(categoryId)" }
            let page: PageResult<Product> = try await client.request(Endpoints.products, query: query)
            products = page.items
            totalPages = page.totalPages
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    /// 搜索商品（第一页）
    func search(keyword: String) async {
        currentKeyword = keyword
        currentCategoryId = nil
        currentPage = 0
        isLoading = true
        defer { isLoading = false }
        do {
            let page: PageResult<Product> = try await client.request(
                Endpoints.search,
                query: ["q": keyword, "page": "\(currentPage)", "size": "\(AppConfig.pageSize)"]
            )
            products = page.items
            totalPages = page.totalPages
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    /// 加载下一页
    func loadMore() async {
        guard !isLoading, !isLoadingMore, currentPage + 1 < totalPages else { return }
        isLoadingMore = true
        defer { isLoadingMore = false }
        let next = currentPage + 1
        do {
            if currentKeyword.isEmpty {
                var query = ["page": "\(next)", "size": "\(AppConfig.pageSize)", "sort": "sales,desc"]
                if let currentCategoryId { query["categoryId"] = "\(currentCategoryId)" }
                let page: PageResult<Product> = try await client.request(Endpoints.products, query: query)
                products += page.items
            } else {
                let page: PageResult<Product> = try await client.request(
                    Endpoints.search,
                    query: ["q": currentKeyword, "page": "\(next)", "size": "\(AppConfig.pageSize)"]
                )
                products += page.items
            }
            currentPage = next
            totalPages = max(totalPages, next + 1)
        } catch {
            // 加载更多失败静默处理，下次滚动再试
        }
    }
}
