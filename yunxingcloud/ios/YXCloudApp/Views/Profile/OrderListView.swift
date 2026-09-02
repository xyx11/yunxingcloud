import SwiftUI

/// 我的订单列表
struct OrderListView: View {
    @EnvironmentObject private var orderStore: OrderStore
    @EnvironmentObject private var auth: AuthStore

    var body: some View {
        Group {
            if !auth.isLoggedIn {
                EmptyStateView(icon: "list.bullet.rectangle", title: "登录后查看订单", subtitle: "登录即可同步你的订单记录")
            } else if orderStore.isLoading {
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = orderStore.errorMessage {
                VStack(spacing: 12) {
                    Text(error).foregroundStyle(.secondary)
                    Button("重试") {
                        Task { await orderStore.load() }
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(AppConfig.brandRed)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if orderStore.orders.isEmpty {
                EmptyStateView(icon: "list.bullet.rectangle", title: "暂无订单", subtitle: "下单后会显示在这里")
            } else {
                orderList
            }
        }
        .background(AppConfig.pageBackground)
        .navigationTitle("我的订单")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            if auth.isLoggedIn { await orderStore.load() }
        }
        .refreshable { await orderStore.load() }
    }

    private var orderList: some View {
        ScrollView {
            LazyVStack(spacing: 10) {
                ForEach(orderStore.orders, id: \.id) { order in
                    orderCard(order)
                }
            }
            .padding(10)
        }
    }

    private func orderCard(_ order: OrderHead) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text("订单号 \(order.orderNo ?? "-")")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
                Spacer()
                Text(order.statusText)
                    .font(.caption)
                    .fontWeight(.semibold)
                    .foregroundStyle(order.status == "0" ? AppConfig.brandRed : .secondary)
            }

            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    if let receiver = order.receiverName, !receiver.isEmpty {
                        Text("收货人：\(receiver)")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                    if let time = order.createdAt, !time.isEmpty {
                        Text("下单时间：\(String(time.prefix(19)).replacingOccurrences(of: "T", with: " "))")
                            .font(.caption2)
                            .foregroundStyle(.tertiary)
                    }
                }
                Spacer()
                PriceText(fen: order.totalAmount ?? 0, size: 16)
            }
        }
        .padding(12)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 10))
    }
}
