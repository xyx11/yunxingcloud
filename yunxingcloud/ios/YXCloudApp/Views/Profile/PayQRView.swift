import SwiftUI
import CoreImage.CIFilterBuiltins

/// 当面付收款二维码页：展示二维码 + 轮询订单状态
struct PayQRView: View {
    @EnvironmentObject private var orderStore: OrderStore
    @Environment(\.dismiss) private var dismiss

    let order: OrderHead
    let qrContent: String
    let channel: String

    @State private var pollTask: Task<Void, Never>?
    @State private var pollCount = 0

    var body: some View {
        VStack(spacing: 20) {
            Spacer()

            Text(channel == "alipay" ? "支付宝扫码支付" : "微信扫码支付")
                .font(.headline)

            if let qrImage = qrCodeImage(from: qrContent) {
                Image(uiImage: qrImage)
                    .interpolation(.none)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 220, height: 220)
                    .padding(12)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .shadow(color: .black.opacity(0.1), radius: 8, y: 2)
            } else {
                Text("二维码生成失败")
                    .foregroundStyle(.secondary)
            }

            PriceText(fen: order.totalAmount ?? 0, size: 22)

            Text("请使用\(channel == "alipay" ? "支付宝" : "微信")扫码完成支付")
                .font(.subheadline)
                .foregroundStyle(.secondary)

            HStack(spacing: 6) {
                ProgressView()
                    .controlSize(.small)
                Text("等待支付完成…（已检查 \(pollCount) 次）")
                    .font(.caption)
                    .foregroundStyle(.tertiary)
            }

            Spacer()

            Button("我已完成支付") {
                Task { await checkOnce() }
            }
            .buttonStyle(.bordered)
            .tint(AppConfig.brandRed)

            Button("取消支付") {
                pollTask?.cancel()
                dismiss()
            }
            .font(.footnote)
            .foregroundStyle(.secondary)
            .padding(.bottom, 30)
        }
        .padding()
        .navigationTitle("收银台")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear { startPolling() }
        .onDisappear { pollTask?.cancel() }
    }

    private func startPolling() {
        pollTask = Task {
            while !Task.isCancelled {
                try? await Task.sleep(for: .seconds(2))
                if Task.isCancelled { return }
                await checkOnce()
            }
        }
    }

    private func checkOnce() async {
        guard let id = order.id else { return }
        pollCount += 1
        if let updated = await orderStore.refreshOrder(id: id), updated.status != "0" {
            pollTask?.cancel()
            orderStore.toastMessage = "支付成功"
            await orderStore.load()
            dismiss()
        }
    }

    private func qrCodeImage(from string: String) -> UIImage? {
        let filter = CIFilter.qrCodeGenerator()
        filter.message = Data(string.utf8)
        guard let output = filter.outputImage else { return nil }
        let scaled = output.transformed(by: CGAffineTransform(scaleX: 10, y: 10))
        let context = CIContext()
        guard let cg = context.createCGImage(scaled, from: scaled.extent) else { return nil }
        return UIImage(cgImage: cg)
    }
}
