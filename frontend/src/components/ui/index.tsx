import React from 'react';

// Re-export HeroUI components
export { 
  Button, 
  Card, 
  CardBody, 
  Input,
  Skeleton,
  Avatar,
  Divider,
  Dropdown,
  DropdownTrigger,
  DropdownMenu,
  DropdownItem,
  Link,
  Tabs,
  Tab,
  Select,
  SelectItem,
  Switch,
  Modal as HeroModal,
  ModalContent,
  ModalHeader,
  ModalBody,
  ModalFooter
} from '@heroui/react';

// Simple wrapper components
export const Alert = ({ children, type = 'info', title, description, className = '', ...props }: any) => (
  <div className={`p-4 rounded-lg border ${className}`} {...props}>
    {title && <h4 className="font-semibold mb-2">{title}</h4>}
    {description && <p className="text-sm">{description}</p>}
    {children}
  </div>
);

export const Modal = ({ children, isOpen, onClose, ...props }: any) => {
  if (!isOpen) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50" onClick={onClose}>
      <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4" onClick={(e) => e.stopPropagation()}>
        {children}
      </div>
    </div>
  );
};

export const Chip = ({ children, color = 'primary', size = 'md', ...props }: any) => (
  <span className={`px-2 py-1 rounded-full text-xs ${color === 'primary' ? 'bg-blue-500 text-white' : 'bg-gray-500 text-white'}`} {...props}>
    {children}
  </span>
); 