import SwiftUI

/// 区块标题：左侧红色竖条 + 标题 + 可选右侧动作
struct SectionHeader: View {
    let title: String
    var actionTitle: String?
    var onAction: (() -> Void)?

    var body: some View {
        HStack {
            Rectangle()
                .fill(AppConfig.brandRed)
                .frame(width: 3, height: 16)
            Text(title)
                .font(.headline)
            Spacer()
            if let actionTitle {
                Button(actionTitle, action: { onAction?() })
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
        }
        .padding(.horizontal)
    }
}
