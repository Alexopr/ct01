import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { Icon } from "@iconify/react";
import { 
  Input, 
  Button, 
  Dropdown, 
  DropdownTrigger, 
  DropdownMenu, 
  DropdownItem, 
  Avatar
} from "@nextui-org/react";

interface TopbarProps {
  onToolPanelToggle: () => void;
  setAuthModalOpen: (open: boolean) => void;
  onMobileMenuToggle: () => void;
}

export const Topbar: React.FC<TopbarProps> = ({
  onToolPanelToggle,
  setAuthModalOpen,
  onMobileMenuToggle
}) => {
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
  };

  return (
    <header className="bg-content1/80 backdrop-blur-lg border-b border-divider/20 px-6 py-4 sticky top-0 z-40">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          {/* Mobile menu button */}
          <Button
            isIconOnly
            variant="light"
            size="sm"
            onPress={onMobileMenuToggle}
            className="md:hidden text-foreground-600 hover:text-foreground-900"
          >
            <Icon icon="solar:hamburger-menu-bold" width={20} height={20} />
          </Button>

          {/* Search */}
          <div className="hidden sm:block">
            <Input
              startContent={<Icon icon="solar:magnifer-linear" className="text-foreground-500" />}
              placeholder="Поиск..."
              variant="flat"
              size="sm"
              className="w-80"
              classNames={{
                inputWrapper: "bg-content2/50 border border-divider/20"
              }}
            />
          </div>
        </div>

        <div className="flex items-center gap-3">
          {/* Tools toggle */}
          <Button
            isIconOnly
            variant="light"
            size="sm"
            onPress={onToolPanelToggle}
            className="text-foreground-600 hover:text-foreground-900"
          >
            <Icon icon="solar:widget-6-bold" width={20} height={20} />
          </Button>

          {/* Notifications */}
          <Button
            isIconOnly
            variant="light"
            size="sm"
            className="text-foreground-600 hover:text-foreground-900 relative"
          >
            <Icon icon="solar:bell-bold" width={20} height={20} />
            <div className="absolute -top-1 -right-1 w-2 h-2 bg-danger rounded-full"></div>
          </Button>

          {/* User menu */}
          {user ? (
            <Dropdown placement="bottom-end">
              <DropdownTrigger>
                <Avatar
                  as="button"
                  size="sm"
                  name={user.username}
                  className="cursor-pointer"
                />
              </DropdownTrigger>
              <DropdownMenu
                aria-label="User Actions"
                className="w-56"
                classNames={{
                  base: "bg-content1/90 backdrop-blur-lg border border-divider/20"
                }}
              >
                <DropdownItem
                  key="profile"
                  startContent={<Icon icon="solar:user-bold" width={16} height={16} />}
                >
                  Профиль
                </DropdownItem>
                <DropdownItem
                  key="settings"
                  startContent={<Icon icon="solar:settings-bold" width={16} height={16} />}
                >
                  Настройки
                </DropdownItem>
                <DropdownItem
                  key="logout"
                  color="danger"
                  className="text-danger"
                  startContent={<Icon icon="solar:logout-3-bold" width={16} height={16} />}
                  onPress={handleLogout}
                >
                  Выйти
                </DropdownItem>
              </DropdownMenu>
            </Dropdown>
          ) : (
            <Button
              variant="bordered"
              size="sm"
              onPress={() => setAuthModalOpen(true)}
              className="border-primary text-primary hover:bg-primary/10"
              startContent={<Icon icon="solar:login-3-bold" width={16} height={16} />}
            >
              Войти
            </Button>
          )}
        </div>
      </div>
    </header>
  );
}; 
