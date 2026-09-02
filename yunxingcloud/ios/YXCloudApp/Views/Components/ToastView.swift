import SwiftUI

/// 全局轻提示：底部悬浮胶囊
struct ToastView: View {
    let message: String

    var body: some View {
        Text(message)
            .font(.subheadline)
            .foregroundStyle(.white)
            .padding(.horizontal, 20)
            .padding(.vertical, 12)
            .background(Color.black.opacity(0.78))
            .clipShape(Capsule())
            .shadow(color: .black.opacity(0.15), radius: 8, y: 3)
            .padding(.bottom, 70)
            .transition(.move(edge: .bottom).combined(with: .opacity))
    }
}
