import SwiftUI

/// 分类页：左侧分类栏 + 右侧商品网格
struct CategoryView: View {
    @Environment(CatalogStore.self) private var catalog
    @State private var selectedCategory: Category?

    var body: some View {
        NavigationStack {
            HStack(spacing: 0) {
                categoryList
                    .frame(width: 100)
                    .background(Color(.systemGray6))

                productArea
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(AppConfig.pageBackground)
            }
            .navigationTitle("分类")
            .navigationBarTitleDisplayMode(.inline)
        }
        .task {
            if catalog.categories.isEmpty { await catalog.loadCategories() }
            if catalog.products.isEmpty { await catalog.loadProducts(categoryId: selectedCategory?.id) }
        }
    }

    private var categoryList: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                ForEach(catalog.topLevelCategories) { cat in
                    Button {
                        selectedCategory = cat
                        Task { await catalog.loadProducts(categoryId: cat.id) }
                    } label: {
                        Text(cat.name)
                            .font(.system(size: 13))
                            .foregroundStyle(selectedCategory?.id == cat.id ? AppConfig.brandRed : .primary)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(selectedCategory?.id == cat.id ? Color.white : Color.clear)
                    }
                }
            }
        }
    }

    private var productArea: some View {
        Group {
            if catalog.isLoading {
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = catalog.errorMessage {
                VStack(spacing: 12) {
                    Text(error).foregroundStyle(.secondary)
                    Button("重试") {
                        Task { await catalog.loadProducts(categoryId: selectedCategory?.id) }
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(AppConfig.brandRed)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if catalog.products.isEmpty {
                Text("该分类暂无商品")
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                productGrid
            }
        }
    }

    private var productGrid: some View {
        ScrollView {
            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 8), count: 2), spacing: 12) {
                ForEach(catalog.products) { product in
                    NavigationLink {
                        ProductDetailView(productId: product.id)
                    } label: {
                        ProductCard(product: product)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(10)
        }
    }
}
