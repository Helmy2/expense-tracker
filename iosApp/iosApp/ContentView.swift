import SwiftUI

struct ContentView: View {
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            ExpenseListView(selectedTab: $selectedTab)
                .tabItem {
                    Label("nav_expenses", systemImage: "receipt")
                }
                .tag(0)

            BudgetListView()
                .tabItem {
                    Label("nav_budgets", systemImage: "wallet.pass")
                }
                .tag(1)

            RecurringListView()
                .tabItem {
                    Label("nav_recurring", systemImage: "clock")
                }
                .tag(2)
        }
        .dreamTheme()
    }
}

#Preview {
    ContentView()
}
