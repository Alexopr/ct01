import React from "react";
import { Link, useLocation } from "react-router-dom";
import { Icon } from "@iconify/react";
import { Button } from "@heroui/react";

interface SidebarProps {
  isAdmin: boolean;
}

const Sidebar: React.FC<SidebarProps> = ({ isAdmin }) => {
  const location = useLocation();
  
  const isActive = (path: string) => {
    return location.pathname === path;
  };
  
  const menuItems = [
    { path: "/", label: "Dashboard", icon: "lucide:layout-dashboard" },
    { path: "/analytics", label: "Analytics", icon: "lucide:bar-chart-2" },
    { path: "/portfolio", label: "Portfolio", icon: "lucide:briefcase" },
    { path: "/news", label: "News", icon: "lucide:newspaper" },
    { path: "/settings", label: "Settings", icon: "lucide:settings" },
  ];

  return (
    <div className="w-64 border-r border-divider bg-content1 overflow-y-auto flex flex-col">
      <div className="p-4 border-b border-divider">
        <h2 className="text-lg font-semibold">Tools</h2>
      </div>
      
      <div className="flex-1 p-2">
        {menuItems.map((item) => (
          <Link to={item.path} key={item.path}>
            <Button
              variant="flat"
              color={isActive(item.path) ? "primary" : "default"}
              className="w-full justify-start mb-2 bg-transparent hover:bg-content2"
              startContent={<Icon icon={item.icon} width={20} height={20} className={isActive(item.path) ? "text-primary" : "text-default-500"} />}
            >
              {item.label}
            </Button>
          </Link>
        ))}
      </div>
    </div>
  );
};

export default Sidebar;