import React from "react";
import { Card, CardBody, CardHeader, CardFooter, Button, Progress } from "@heroui/react";
import { Icon } from "@iconify/react";

const Dashboard: React.FC = () => {
  return (
    <div className="flex flex-col md:flex-row h-full">
      {/* Left panel - Bitcoin card */}
      <div className="w-full md:w-1/4 p-4 border-r border-divider">
        <div className="bg-content1 rounded-lg p-4 h-full">
          <div className="mb-4">
            <h3 className="text-xl font-bold">Bitcoin (BTC)</h3>
          </div>
          
          <div className="text-4xl font-bold mb-2">$48,235.12</div>
          
          <div className="flex items-center text-success mb-8">
            <span className="text-xl">+5.2%</span>
          </div>
          
          <div className="mt-auto">
            <div className="flex justify-between text-small mb-1">
              <span>24h Volume</span>
              <span>$32.4B</span>
            </div>
            <Progress value={65} color="primary" className="h-2" />
          </div>
          
          <div className="mt-8">
            <Button color="primary" variant="flat" fullWidth>
              View Details
            </Button>
          </div>
        </div>
      </div>
      
      {/* Right panel - Tools */}
      <div className="w-full md:w-3/4 p-4">
        <h2 className="text-2xl font-bold mb-4">Tools</h2>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Button 
            className="h-20 bg-content1 hover:bg-content2 justify-start px-4"
            startContent={<Icon icon="lucide:layout-dashboard" className="text-primary" width={24} height={24} />}
          >
            <span className="ml-2 text-lg">Dashboard</span>
          </Button>
          
          <Button 
            className="h-20 bg-content1 hover:bg-content2 justify-start px-4"
            startContent={<Icon icon="lucide:bar-chart-2" className="text-default-500" width={24} height={24} />}
          >
            <span className="ml-2 text-lg">Analytics</span>
          </Button>
          
          <Button 
            className="h-20 bg-content1 hover:bg-content2 justify-start px-4"
            startContent={<Icon icon="lucide:briefcase" className="text-default-500" width={24} height={24} />}
          >
            <span className="ml-2 text-lg">Portfolio</span>
          </Button>
          
          <Button 
            className="h-20 bg-content1 hover:bg-content2 justify-start px-4"
            startContent={<Icon icon="lucide:newspaper" className="text-default-500" width={24} height={24} />}
          >
            <span className="ml-2 text-lg">News</span>
          </Button>
          
          <Button 
            className="h-20 bg-content1 hover:bg-content2 justify-start px-4"
            startContent={<Icon icon="lucide:settings" className="text-default-500" width={24} height={24} />}
          >
            <span className="ml-2 text-lg">Settings</span>
          </Button>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;