import React from "react";
import { BrowserRouter as Router, Route, Switch } from "react-router-dom";
import { useTheme } from "@heroui/use-theme";
import { Icon } from "@iconify/react";
import { Navbar, NavbarContent, NavbarItem, NavbarBrand, Button, Dropdown, DropdownTrigger, DropdownMenu, DropdownItem, Badge, Avatar, User } from "@heroui/react";
import Dashboard from "./components/dashboard";
import Sidebar from "./components/sidebar";
import CryptoTicker from "./components/crypto-ticker";
import { AuthProvider } from "./contexts/auth-context";

const App: React.FC = () => {
  const { theme, setTheme } = useTheme();
  const [isAdmin, setIsAdmin] = React.useState(false);
  
  const toggleTheme = () => {
    setTheme(theme === "light" ? "dark" : "light");
  };

  // For demo purposes only - toggle admin role
  const toggleAdminRole = () => {
    setIsAdmin(!isAdmin);
  };

  return (
    <AuthProvider>
      <Router>
        <div className="flex h-screen bg-background text-foreground">
          {/* Left Sidebar */}
          <Sidebar isAdmin={isAdmin} />
          
          <div className="flex flex-col flex-1 overflow-hidden">
            {/* Top Navbar */}
            <Navbar maxWidth="full" className="border-b border-divider bg-background">
              <NavbarBrand>
                <Icon icon="logos:bitcoin" width={28} height={28} className="text-primary" />
                <p className="font-bold text-inherit ml-2">CryptoDash</p>
              </NavbarBrand>
              
              <NavbarContent justify="end" className="gap-4">
                <NavbarItem>
                  <Dropdown placement="bottom-end">
                    <DropdownTrigger>
                      <Button isIconOnly variant="light" aria-label="Notifications">
                        <Badge content="5" color="danger">
                          <Icon icon="lucide:bell" width={20} height={20} />
                        </Badge>
                      </Button>
                    </DropdownTrigger>
                    <DropdownMenu aria-label="Notifications">
                      <DropdownItem key="new_price_alert">New price alert: BTC +5%</DropdownItem>
                      <DropdownItem key="market_update">Market update available</DropdownItem>
                      <DropdownItem key="new_feature">New feature: Portfolio tracking</DropdownItem>
                    </DropdownMenu>
                  </Dropdown>
                </NavbarItem>
                
                <NavbarItem>
                  <Button isIconOnly variant="light" aria-label="Theme" onPress={toggleTheme}>
                    <Icon icon="lucide:sun" width={20} height={20} />
                  </Button>
                </NavbarItem>
                
                <NavbarItem>
                  <Avatar
                    src="https://img.heroui.chat/image/avatar?w=40&h=40&u=1"
                    className="transition-transform"
                    size="sm"
                  />
                </NavbarItem>
              </NavbarContent>
            </Navbar>
            
            {/* Main Content Area */}
            <div className="flex-1 overflow-auto p-0 bg-background">
              <Switch>
                <Route path="/" exact component={Dashboard} />
                <Route path="/exchanges" render={() => <div className="p-4"><h1 className="text-2xl font-bold">Exchanges</h1><p className="mt-4">Exchanges content will be displayed here.</p></div>} />
                <Route path="/twitter" render={() => <div className="p-4"><h1 className="text-2xl font-bold">Twitter Scan</h1><p className="mt-4">Twitter scan content will be displayed here.</p></div>} />
                <Route path="/pumps" render={() => <div className="p-4"><h1 className="text-2xl font-bold">PumpFun Scan</h1><p className="mt-4">PumpFun scan content will be displayed here.</p></div>} />
                <Route path="/telegram" render={() => <div className="p-4"><h1 className="text-2xl font-bold">Telegram Scan</h1><p className="mt-4">Telegram scan content will be displayed here.</p></div>} />
                <Route path="/dexscreener" render={() => <div className="p-4"><h1 className="text-2xl font-bold">DexScreener</h1><p className="mt-4">DexScreener content will be displayed here.</p></div>} />
                <Route path="/admin" render={() => <div className="p-4"><h1 className="text-2xl font-bold">Admin Panel</h1><p className="mt-4">{isAdmin ? "Welcome to the admin panel." : "You don't have permission to access this page."}</p></div>} />
              </Switch>
            </div>
            
            {/* Bottom Crypto Ticker */}
            <CryptoTicker />
          </div>
        </div>
      </Router>
    </AuthProvider>
  );
};

export default App;