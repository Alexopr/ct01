// Re-export NextUI components
export { 
  Button, 
  Card, 
  CardBody, 
  CardHeader,
  CardFooter,
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
  Modal,
  ModalContent,
  ModalHeader,
  ModalBody,
  ModalFooter,
  Chip
} from '@nextui-org/react';

// Custom components
export { Button as CustomButton } from './Button';
export { Card as CustomCard } from './Card';
export { Input as CustomInput } from './Input';
export { Modal as CustomModal } from './Modal';

// Simple wrapper components for additional functionality
export const Alert = ({ children, type = 'info', title, description, className = '', ...props }: any) => (
  <div className={`p-4 rounded-lg border ${className}`} {...props}>
    {title && <h4 className="font-semibold mb-2">{title}</h4>}
    {description && <p className="text-sm">{description}</p>}
    {children}
  </div>
); 
