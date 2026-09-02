import SwiftUI

struct CartView: View {
    @EnvironmentObject private var cart: CartStore
    @EnvironmentObject private var auth: AuthStore

    var body: some View {
        NavigationStack {
            Group {
                if !auth.isLoggedIn {
                    notLoggedInView
                } else if cart.isLoading {
                    ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if cart.items.isEmpty {
                    emptyView
                } else {
                    cartList
                }
            }
            .background(AppConfig.pageBackground)
            .navigationTitle("购物车")
            .navigationBarTitleDisplayMode(.inline)
        }
        .task {
            if auth.isLoggedIn { await cart.load() }
        }
        .onChange(of: auth.isLoggedIn) { _ in
            Task { await cart.load() }
        }
    }

    private var notLoggedInView: some View {
        VStack(spacing: 12) {
            EmptyStateView(icon: "cart", title: "登录后查看购物车", subtitle: "登录即可同步你的购物车商品")
                .frame(maxHeight: 200)
            NavigationLink {
                LoginView()
            } label: {
                Text("去登录")
                    .fontWeight(.semibold)
                    .foregroundStyle(.white)
                    .padding(.horizontal, 32)
                    .frame(height: 44)
                    .background(AppConfig.brandRed)
                    .clipShape(Capsule())
            }
            .padding(.bottom, 40)
        }
    }

    private var emptyView: some View {
        EmptyStateView(icon: "cart", title: "购物车还是空的", subtitle: "快去挑选心仪的商品吧")
    }

    private var cartList: some View {
        VStack(spacing: 0) {
            List {
                ForEach(cart.items) { item in
                    HStack(spacing: 12) {
                        RemoteImage(url: ImageURL.resolve(item.imageUrl), cornerRadius: 8)
                            .frame(width: 72, height: 72)

                        VStack(alignment: .leading, spacing: 6) {
                            Text(item.productName ?? "商品")
                                .font(.subheadline)
                                .lineLimit(2)
                            PriceText(fen: item.priceFen, size: 15)
                        }

                        Spacer()

                        Stepper("", value: Binding(
                            get: { item.quantity },
                            set: { newQty in
                                Task { await cart.updateQuantity(item: item, quantity: newQty) }
                            }
                        ), in: 1...99)
                        .labelsHidden()
                    }
                    .swipeActions {
                        Button("删除", role: .destructive) {
                            Task { await cart.remove(item: item) }
                        }
                    }
                }
            }
            .listStyle(.plain)

            bottomBar
        }
    }

    private var bottomBar: some View {
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text("共 \(cart.totalQuantity) 件")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                PriceText(fen: cart.totalPriceFen, size: 20)
            }
            Spacer()
            Button {
                // 结算（MVP 阶段提示）
            } label: {
                Text("去结算")
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundStyle(.white)
                    .padding(.horizontal, 40)
                    .frame(height: 44)
                    .background(AppConfig.brandRed)
                    .clipShape(Capsule())
            }
        }
        .padding(.horizontal)
        .padding(.vertical, 10)
        .background(Color.white)
        .overlay(alignment: .top) { Divider() }
    }
}
