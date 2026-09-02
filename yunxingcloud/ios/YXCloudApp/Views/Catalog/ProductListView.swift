import SwiftUI

/// 通用商品列表页：分类入口 / 搜索结果共用（分页加载更多）
struct ProductListView: View {
    @EnvironmentObject private var catalog: CatalogStore
    let title: String
    var categoryId: Int?

    var body: some View {
        Group {
            if catalog.isLoading {
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = catalog.errorMessage {
                VStack(spacing: 12) {
                    Text(error).foregroundStyle(.secondary)
                    Button("重试") {
                        Task { await catalog.loadProducts(categoryId: categoryId) }
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(AppConfig.brandRed)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if catalog.products.isEmpty {
                Text("暂无商品")
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                grid
            }
        }
        .background(AppConfig.pageBackground)
        .navigationTitle(title)
        .navigationBarTitleDisplayMode(.inline)
        .task { await catalog.loadProducts(categoryId: categoryId) }
    }

    private var grid: some View {
        ScrollView {
            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 8), count: 2), spacing: 12) {
                ForEach(catalog.products) { product in
                    NavigationLink {
                        ProductDetailView(productId: product.id)
                    } label: {
                        ProductCard(product: product)
                    }
                    .buttonStyle(.plain)
                    .onAppear {
                        if product.id == catalog.products.last?.id {
                            Task { await catalog.loadMore() }
                        }
                    }
                }
            }
            .padding(10)

            if catalog.isLoadingMore {
                ProgressView().padding(.vertical)
            }
        }
        .refreshable { await catalog.loadProducts(categoryId: categoryId) }
    }
}

/// 搜索页：搜索框 + 结果列表
struct SearchView: View {
    @EnvironmentObject private var catalog: CatalogStore
    @State private var keyword = ""
    @State private var searched = ""

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 8) {
                TextField("搜索商品", text: $keyword)
                    .textFieldStyle(.roundedBorder)
                    .submitLabel(.search)
                    .onSubmit { doSearch() }
                Button("搜索", action: doSearch)
                    .buttonStyle(.borderedProminent)
                    .tint(AppConfig.brandRed)
            }
            .padding(.horizontal)
            .padding(.vertical, 8)
            .background(Color.white)

            if searched.isEmpty {
                EmptyStateView(icon: "magnifyingglass", title: "输入关键词搜索商品")
            } else {
                resultList
            }
        }
        .navigationTitle("搜索")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func doSearch() {
        let kw = keyword.trimmingCharacters(in: .whitespaces)
        guard !kw.isEmpty else { return }
        searched = kw
        Task { await catalog.search(keyword: kw) }
    }

    private var resultList: some View {
        Group {
            if catalog.isLoading {
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if catalog.products.isEmpty {
                EmptyStateView(icon: "tray", title: "没有找到相关商品")
            } else {
                ScrollView {
                    LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 8), count: 2), spacing: 12) {
                        ForEach(catalog.products) { product in
                            NavigationLink {
                                ProductDetailView(productId: product.id)
                            } label: {
                                ProductCard(product: product)
                            }
                            .buttonStyle(.plain)
                            .onAppear {
                                if product.id == catalog.products.last?.id {
                                    Task { await catalog.loadMore() }
                                }
                            }
                        }
                    }
                    .padding(10)
                }
            }
        }
        .background(AppConfig.pageBackground)
    }
}
