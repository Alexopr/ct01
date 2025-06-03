import React, { useState, useEffect } from 'react';
import Sidebar from './Sidebar';
import { TelegramAuthModal } from './auth';
import { ToolPanel } from './Layout/ToolPanel';
import { Topbar } from './Layout/Topbar';

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
