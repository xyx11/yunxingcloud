import SwiftUI

struct HomeView: View {
    @Environment(HomeStore.self) private var home
    @State private var bannerIndex = 0

    var body: some View {
        NavigationStack {
            Group {
                if home.isLoading {
                    ProgressView("加载中...").frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let error = home.errorMessage {
                    errorView(error)
                } else if let data = home.data {
                    content(data)
                } else {
                    errorView(nil)
                }
            }
            .background(AppConfig.pageBackground)
            .navigationTitle("YXCLOUD 商城")
            .navigationBarTitleDisplayMode(.inline)
        }
        .task { await home.load() }
    }

    private func content(_ data: HomeData) -> some View {
        ScrollView {
            VStack(spacing: 16) {
                searchBar
                if !data.usableBanners.isEmpty {
                    bannerCarousel(data.usableBanners)
                }
                if !data.topLevelCategories.isEmpty {
                    categoryGrid(data.topLevelCategories)
                }
                if !data.hotProducts.isEmpty {
                    SectionHeader(title: "热销爆款")
                    productGrid(data.hotProducts)
                }
                if !data.newProducts.isEmpty {
                    SectionHeader(title: "新品首发")
                    productGrid(data.newProducts)
                }
            }
            .padding(.vertical, 8)
        }
        .refreshable { await home.load(force: true) }
    }

    private var searchBar: some View {
        NavigationLink {
            SearchView()
        } label: {
            HStack(spacing: 6) {
                Image(systemName: "magnifyingglass")
                Text("搜索商品")
                Spacer()
            }
            .font(.subheadline)
            .foregroundStyle(.secondary)
            .padding(.horizontal, 12)
            .frame(height: 36)
            .background(Color.white)
            .clipShape(Capsule())
            .padding(.horizontal)
        }
    }

    private func bannerCarousel(_ banners: [Banner]) -> some View {
        VStack(spacing: 8) {
            TabView(selection: $bannerIndex) {
                ForEach(Array(banners.enumerated()), id: \.offset) { index, banner in
                    RemoteImage(url: ImageURL.resolve(banner.imageUrl), cornerRadius: 10)
                        .tag(index)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
            .frame(height: 150)
            .padding(.horizontal)

            HStack(spacing: 6) {
                ForEach(banners.indices, id: \.self) { i in
                    Circle()
                        .fill(i == bannerIndex ? AppConfig.brandRed : Color.gray.opacity(0.3))
                        .frame(width: 6, height: 6)
                }
            }
        }
    }

    private func categoryGrid(_ categories: [Category]) -> some View {
        LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 5), spacing: 12) {
            ForEach(categories) { cat in
                NavigationLink {
                    ProductListView(title: cat.name, categoryId: cat.id)
                } label: {
                    VStack(spacing: 6) {
                        Text(cat.icon ?? "🛍️")
                            .font(.title2)
                        Text(cat.name)
                            .font(.system(size: 11))
                            .foregroundStyle(.primary)
                            .lineLimit(1)
                    }
                }
            }
        }
        .padding(.horizontal)
        .padding(.vertical, 12)
        .background(Color.white)
    }

    private func productGrid(_ products: [Product]) -> some View {
        LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 8), count: 2), spacing: 12) {
            ForEach(products.prefix(10)) { product in
                NavigationLink {
                    ProductDetailView(productId: product.id)
                } label: {
                    ProductCard(product: product)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal)
    }

    private func errorView(_ message: String?) -> some View {
        VStack(spacing: 12) {
            Image(systemName: "wifi.exclamationmark")
                .font(.largeTitle)
                .foregroundStyle(.secondary)
            Text(message ?? "加载失败")
                .foregroundStyle(.secondary)
            Button("重新加载") {
                Task { await home.load(force: true) }
            }
            .buttonStyle(.borderedProminent)
            .tint(AppConfig.brandRed)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
