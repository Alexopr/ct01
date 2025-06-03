import React, { useEffect } from 'react';
import { Icon } from "@iconify/react";
import { 
  Button, 
  Modal,
  ModalContent,
  ModalHeader,
  ModalBody,
  useDisclosure,
  Skeleton
} from "@nextui-org/react";

interface Tool {
  key: string;
  label: string;
  icon: string;
  component: React.FC;
}

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
    <p className="text-foreground-600">Настройки и конфигурация системы.</p>
  </div>
);

const StatsTool: React.FC = () => (
  <div>
    <h3 className="text-lg font-semibold mb-3">Statistics Tool</h3>
    <p className="text-foreground-600">Статистика использования и метрики.</p>
  </div>
);

const tools: Tool[] = [
  {
    key: 'info',
    label: 'Information',
    icon: 'solar:info-circle-bold',
    component: InfoTool
  },
  {
    key: 'settings',
    label: 'Settings',
    icon: 'solar:settings-bold',
    component: SettingsTool
  },
  {
    key: 'stats',
    label: 'Statistics',
    icon: 'solar:chart-square-bold',
    component: StatsTool
  }
];

interface ToolPanelProps {
  isOpen: boolean;
  onClose: () => void;
  selectedTool: string;
  setSelectedTool: (tool: string) => void;
  toolLoading: boolean;
}

export const ToolPanel: React.FC<ToolPanelProps> = ({ 
  isOpen, 
  onClose, 
  selectedTool, 
  setSelectedTool, 
  toolLoading 
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
