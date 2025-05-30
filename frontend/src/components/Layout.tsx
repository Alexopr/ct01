import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import Sidebar from './Sidebar';
import { TelegramAuthModal } from './auth';
import { Icon } from "@iconify/react";
import { 
  Input, 
  Button, 
  Dropdown, 
  DropdownTrigger, 
  DropdownMenu, 
  DropdownItem, 
  Avatar,
  Card,
  CardBody,
  Skeleton,
  Modal,
  ModalContent,
  ModalHeader,
  ModalBody,
  useDisclosure
} from "@heroui/react";

// Tool components
const InfoTool: React.FC = () => (
  <div>
    <h3 className="text-lg font-semibold mb-3">Info Tool</h3>
    <p className="text-foreground-600">Это тестовый информационный инструмент.</p>
  </div>
);

const SettingsTool: React.FC = () => (
  <div>
    <h3 className="text-lg font-semibold mb-3">Settings Tool</h3>
    <p className="text-foreground-600">Это тестовый инструмент настроек.</p>
  </div>
);

const HelpTool: React.FC = () => (
  <div>
    <h3 className="text-lg font-semibold mb-3">Help Tool</h3>
    <p className="text-foreground-600">Это тестовый инструмент помощи.</p>
  </div>
);

interface Tool {
  key: string;
  label: string;
  icon: string;
  component: React.FC;
}

const tools: Tool[] = [
  { key: 'info', label: 'Инфо', icon: 'solar:info-circle-linear', component: InfoTool },
  { key: 'settings', label: 'Настройки', icon: 'solar:settings-linear', component: SettingsTool },
  { key: 'help', label: 'Помощь', icon: 'solar:question-circle-linear', component: HelpTool },
];

const Topbar = ({ 
  onToolPanelToggle,
  authModalOpen, 
  setAuthModalOpen,
  onMobileMenuToggle 
}: { 
  onToolPanelToggle: () => void;
  authModalOpen: boolean;
  setAuthModalOpen: (open: boolean) => void;
  onMobileMenuToggle: () => void;
}) => {
  const { logout, isAuthenticated, loading, user } = useAuth();

  const userMenuItems = [];
  
  if (!isAuthenticated()) {
    userMenuItems.push(
      <DropdownItem key="login" onPress={() => setAuthModalOpen(true)}>
        <div className="flex items-center gap-2">
          <Icon icon="solar:login-3-bold" width={16} height={16} />
          Войти
        </div>
      </DropdownItem>
    );
  }
  
  if (isAuthenticated()) {
    userMenuItems.push(
      <DropdownItem key="profile" href="/profile">
        <div className="flex items-center gap-2">
          <Icon icon="solar:user-bold" width={16} height={16} />
          Профиль
        </div>
      </DropdownItem>,
      <DropdownItem key="settings" href="/settings">
        <div className="flex items-center gap-2">
          <Icon icon="solar:settings-bold" width={16} height={16} />
          Настройки
        </div>
      </DropdownItem>,
      <DropdownItem key="logout" onPress={logout} className="text-danger">
        <div className="flex items-center gap-2">
          <Icon icon="solar:logout-3-bold" width={16} height={16} />
          Выйти
        </div>
      </DropdownItem>
    );
  }

  return (
    <div className="w-full h-16 flex items-center px-6 py-3 gap-4 relative z-30 border-b border-content3 backdrop-blur-lg bg-content1/95 supports-[backdrop-filter]:bg-content1/90">
      {/* Mobile Menu Button */}
      <Button
        isIconOnly
        variant="light"
        size="sm"
        onPress={onMobileMenuToggle}
        className="md:hidden text-foreground-600 hover:text-primary hover:bg-content2/50 transition-all duration-200"
      >
        <Icon icon="solar:hamburger-menu-bold" width={20} height={20} />
      </Button>

      {/* Logo */}
      <div className="flex items-center gap-3">
        <div className="w-8 h-8 bg-gradient-to-r from-primary to-secondary rounded-lg flex items-center justify-center text-white font-black text-lg">
          C
        </div>
        <h1 className="text-lg font-bold text-gradient-primary tracking-wide">
          Crypto Dashboard
        </h1>
      </div>

      {/* Search */}
      <div className="flex-1 flex justify-center max-w-md hidden sm:flex">
        <Input
          placeholder="Поиск монет, бирж..."
          startContent={<Icon icon="solar:magnifer-bold" width={18} height={18} />}
          className="w-full"
          variant="bordered"
          size="sm"
          classNames={{
            inputWrapper: "backdrop-blur-sm bg-content2/30 border-content3 hover:border-primary/60 focus-within:border-primary transition-all duration-200"
          }}
        />
      </div>

      {/* Right Side Icons */}
      <div className="flex items-center gap-1">
        <Button
          isIconOnly
          variant="light"
          size="sm"
          as="a"
          href="/notifications"
          className="text-foreground-600 hover:text-primary hover:bg-content2/50 transition-all duration-200"
        >
          <Icon icon="solar:bell-bing-bold" width={18} height={18} />
        </Button>

        <Dropdown>
          <DropdownTrigger>
            <Button
              isIconOnly
              variant="light"
              size="sm"
              className="text-foreground-600 hover:text-primary hover:bg-content2/50 transition-all duration-200"
            >
              <Icon icon="solar:settings-bold" width={18} height={18} />
            </Button>
          </DropdownTrigger>
          <DropdownMenu 
            aria-label="Settings"
            className="backdrop-blur-lg bg-content1/95"
          >
            <DropdownItem key="settings" href="/settings">
              <div className="flex items-center gap-2">
                <Icon icon="solar:settings-bold" width={16} height={16} />
                Настройки
              </div>
            </DropdownItem>
            <DropdownItem key="users" href="/admin/users">
              <div className="flex items-center gap-2">
                <Icon icon="solar:users-group-rounded-bold" width={16} height={16} />
                Управление пользователями
              </div>
            </DropdownItem>
            <DropdownItem key="help" href="/about">
              <div className="flex items-center gap-2">
                <Icon icon="solar:question-circle-bold" width={16} height={16} />
                Помощь и поддержка
              </div>
            </DropdownItem>
          </DropdownMenu>
        </Dropdown>

        <Button
          isIconOnly
          variant="light"
          size="sm"
          onPress={onToolPanelToggle}
          className="text-foreground-600 hover:text-primary hover:bg-content2/50 transition-all duration-200"
        >
          <Icon icon="solar:widget-4-bold" width={18} height={18} />
        </Button>

        {!loading && (
          <Dropdown>
            <DropdownTrigger>
              <Button
                isIconOnly
                variant="ghost"
                size="sm"
                className="text-primary border-1 border-primary/20 hover:border-primary/40 transition-all duration-200"
              >
                {isAuthenticated() && user ? (
                  <Avatar
                    src={user.photoUrl}
                    name={user.firstName?.[0] || user.username?.[0] || 'U'}
                    size="sm"
                    className="w-6 h-6"
                  />
                ) : (
                  <Icon icon="solar:user-bold" width={18} height={18} />
                )}
              </Button>
            </DropdownTrigger>
            <DropdownMenu 
              aria-label="User actions"
              className="backdrop-blur-lg bg-content1/95"
            >
              {userMenuItems}
            </DropdownMenu>
          </Dropdown>
        )}
      </div>
    </div>
  );
};

const ToolPanel = ({ 
  isOpen, 
  onClose, 
  selectedTool, 
  setSelectedTool, 
  toolLoading 
}: {
  isOpen: boolean;
  onClose: () => void;
  selectedTool: string;
  setSelectedTool: (tool: string) => void;
  toolLoading: boolean;
}) => {
  const { isOpen: modalIsOpen, onOpenChange } = useDisclosure();

  useEffect(() => {
    if (isOpen !== modalIsOpen) {
      onOpenChange();
    }
  }, [isOpen, modalIsOpen, onOpenChange]);

  return (
    <Modal 
      isOpen={isOpen} 
      onClose={onClose}
      placement="top-center"
      size="3xl"
      backdrop="blur"
      classNames={{
        backdrop: "backdrop-blur-sm",
        base: "mt-16 mr-6",
        wrapper: "justify-end items-start"
      }}
    >
      <ModalContent className="backdrop-blur-lg bg-content1/90 border border-divider/20">
        <ModalHeader className="flex justify-between items-center border-b border-divider/20">
          <span className="text-lg font-semibold">
            {tools.find(t => t.key === selectedTool)?.label}
          </span>
          <Button
            isIconOnly
            variant="light"
            size="sm"
            onPress={onClose}
            className="text-foreground-600 hover:text-danger"
          >
            <Icon icon="solar:close-circle-linear" width={20} height={20} />
          </Button>
        </ModalHeader>
        <ModalBody className="p-0">
          <div className="flex h-96">
            {/* Tool Selector */}
            <div className="w-16 bg-content2/30 border-r border-divider/20 flex flex-col items-center py-4 gap-2">
              {tools.map(tool => (
                <Button
                  key={tool.key}
                  isIconOnly
                  variant={selectedTool === tool.key ? "solid" : "light"}
                  color={selectedTool === tool.key ? "primary" : "default"}
                  size="sm"
                  onPress={() => setSelectedTool(tool.key)}
                  className={`transition-all duration-200 ${
                    selectedTool === tool.key 
                      ? 'bg-primary text-primary-foreground shadow-lg' 
                      : 'text-foreground-600 hover:text-foreground-900 hover:bg-content3/50'
                  }`}
                >
                  <Icon icon={tool.icon} width={18} height={18} />
                </Button>
              ))}
            </div>
            
            {/* Tool Content */}
            <div className="flex-1 p-6">
              {toolLoading ? (
                <div className="space-y-3">
                  <Skeleton className="h-6 w-48 rounded-lg" />
                  <Skeleton className="h-4 w-full rounded-lg" />
                  <Skeleton className="h-4 w-3/4 rounded-lg" />
                </div>
              ) : (
                React.createElement(tools.find(t => t.key === selectedTool)?.component || InfoTool)
              )}
            </div>
          </div>
        </ModalBody>
      </ModalContent>
    </Modal>
  );
};

const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [toolPanelOpen, setToolPanelOpen] = useState(false);
  const [selectedTool, setSelectedTool] = useState<string>('info');
  const [toolLoading, setToolLoading] = useState(false);
  const [authModalOpen, setAuthModalOpen] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    if (toolPanelOpen) {
      setToolLoading(true);
      const timer = setTimeout(() => setToolLoading(false), 400);
      return () => clearTimeout(timer);
    }
  }, [toolPanelOpen, selectedTool]);

  const handleToolPanelToggle = () => {
    setToolPanelOpen(!toolPanelOpen);
  };

  const handleMobileMenuToggle = () => {
    setMobileMenuOpen(!mobileMenuOpen);
  };

  return (
    <div className="min-h-screen bg-anthropic-dark">
      <Sidebar 
        isOpen={mobileMenuOpen}
        onClose={() => setMobileMenuOpen(false)}
      />
      
      <div className="ml-0 md:ml-56">
        <Topbar
          onToolPanelToggle={handleToolPanelToggle}
          authModalOpen={authModalOpen}
          setAuthModalOpen={setAuthModalOpen}
          onMobileMenuToggle={handleMobileMenuToggle}
        />
        
        <main className="p-6 pb-20 bg-anthropic-dark">
        {children}
        </main>
      </div>

      <ToolPanel
        isOpen={toolPanelOpen}
        onClose={() => setToolPanelOpen(false)}
        selectedTool={selectedTool}
        setSelectedTool={setSelectedTool}
        toolLoading={toolLoading}
      />

      <TelegramAuthModal
        open={authModalOpen}
        onClose={() => setAuthModalOpen(false)}
      />
    </div>
  );
};

export default Layout; 