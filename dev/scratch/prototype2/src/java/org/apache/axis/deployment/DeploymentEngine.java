package org.apache.axis.deployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axis.context.MessageContext;
import org.apache.axis.deployment.metadata.FlowMetaData;
import org.apache.axis.deployment.metadata.HandlerMetaData;
import org.apache.axis.deployment.metadata.ModuleMetaData;
import org.apache.axis.deployment.metadata.OperationMetaData;
import org.apache.axis.deployment.metadata.ParameterMetaData;
import org.apache.axis.deployment.metadata.ServerMetaData;
import org.apache.axis.deployment.metadata.ServiceMetaData;
import org.apache.axis.deployment.metadata.phaserule.PhaseException;
import org.apache.axis.deployment.repository.utill.HDFileItem;
import org.apache.axis.deployment.repository.utill.UnZipJAR;
import org.apache.axis.deployment.repository.utill.WSInfo;
import org.apache.axis.deployment.scheduler.DeploymentIterator;
import org.apache.axis.deployment.scheduler.Scheduler;
import org.apache.axis.deployment.scheduler.SchedulerTask;
import org.apache.axis.description.AxisGlobal;
import org.apache.axis.description.AxisModule;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.Flow;
import org.apache.axis.description.Parameter;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.Constants;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.engine.ExecutionChain;
import org.apache.axis.engine.Handler;
import org.apache.axis.engine.Phase;
import org.apache.axis.impl.description.FlowImpl;
import org.apache.axis.impl.description.ParameterImpl;
import org.apache.axis.impl.description.SimpleAxisOperationImpl;
import org.apache.axis.impl.description.SimpleAxisServiceImpl;
import org.apache.axis.impl.engine.EngineRegistryImpl;
import org.apache.axis.impl.providers.SimpleJavaProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Deepal Jayasinghe
 *         Oct 13, 2004
 *         12:33:17 PM
 */
public class DeploymentEngine implements DeploymentConstants {
    private Log log = LogFactory.getLog(getClass());
    private final Scheduler scheduler = new Scheduler();
    /**
     * This will store all the web Services to deploye
     */
    private Vector wsToDeploy = new Vector();
    /**
     * this will store all the web Services to undeploye
     */
    private Vector wsToUnDeploy = new Vector();

    /**
     * to keep a ref to engine register
     * this ref will pass to engine when it call start()
     * method
     */
    private EngineRegistry engineRegistry = null;

    private Vector modulelist = new Vector();

    private Vector servicelist = new Vector();
    /**
     * this constaructor for the testing
     */

    private String folderName;

    /**
     * This to keep a referance to serverMetaData object
     */
    private static ServerMetaData server = new ServerMetaData();


    private HDFileItem currentFileItem = null;

    /**
     * This the constructor which is used by Engine inorder to start
     * Deploymenat module,
     *
     * @param RepositaryName is the path to which Repositary Listner should
     *                       listent.
     */
    public DeploymentEngine(String RepositaryName) {
        this.folderName = RepositaryName;
    }


    /**
     * This method should use inorder to get the referance to servermetadata
     * object which keep all the detail about all the phases
     *
     * @return
     */
    public static ServerMetaData getServerMetaData() {
        return server;
    }


    /**
     * This method will fill the engine registry and return it to Engine
     *
     * @return
     * @throws AxisFault
     * @throws PhaseException
     */
    public EngineRegistry start() throws AxisFault, PhaseException, DeploymentException, XMLStreamException {
        String fileName = "src/test-resources/deployment/server.xml";
        File tempfile = new File(fileName);
        try {
            InputStream in = new FileInputStream(tempfile);
            DeploymentParser parser = new DeploymentParser(in, this);
            parser.procesServerXML(server);
        } catch (FileNotFoundException e) {
            throw new AxisFault(e.getMessage());
        }
        engineRegistry = createEngineRegistry();
        startSearch(this);
        return engineRegistry;
    }


    public void addService(ServiceMetaData serviceMetaData) throws PhaseException, AxisFault {
        servicelist.add(serviceMetaData);
        addnewService(serviceMetaData);
    }

    public void addModule(ModuleMetaData module) throws AxisFault {
        modulelist.add(module);
        AxisModule simplemodule = castModuleMetaData(module);
        engineRegistry.addMdoule(simplemodule);

    }

    public ModuleMetaData getModule(String moduleName) {
        for (int i = 0; i < modulelist.size(); i++) {
            ModuleMetaData metaData = (ModuleMetaData) modulelist.elementAt(i);
            if (metaData.getName().equals(moduleName)) {
                return metaData;
            }
        }
        return null;
    }

    /**
     * this method use to start the Deployment engine
     * inorder to perform Hot deployment and so on..
     */
    private void startSearch(DeploymentEngine engine) {
        scheduler.schedule(new SchedulerTask(engine, folderName), new DeploymentIterator());
    }

    private EngineRegistry createEngineRegistry() throws AxisFault {
        EngineRegistry newEngineRegisty;
        //todo the below line dse not need ask from srinath why
        // do we need to have this line
        QName transportName = new QName("Transport");

        /**
         * adding Global
         */

        AxisGlobal global = new AxisGlobal();
        newEngineRegisty = new EngineRegistryImpl(global);
        /**
         * adding transport
         */
        FlowImpl transportinflow = new FlowImpl();
        FlowImpl transportoutflow = new FlowImpl();
        FlowImpl transportfaultflow = new FlowImpl();


        /**
         * adding services
         */
        /*   for (int i = 0; i < servicelist.size(); i++) {
        ServiceMetaData serviceMetaData = (ServiceMetaData) servicelist.elementAt(i);
        addnewService(serviceMetaData);
        } */
        return newEngineRegisty;
    }


    private void addnewService(ServiceMetaData serviceMetaData) throws AxisFault, PhaseException {
//        QName serviceName = new QName(serviceMetaData.getName());
//        int count = 0;
//
//        FlowImpl serviceinflow = new FlowImpl();
//        FlowImpl serviceoutflow = new FlowImpl();
//        FlowImpl servicefaultflow = new FlowImpl();
//
//        AxisService service = new SimpleAxisServiceImpl(serviceName);
//        service.setInFlow(serviceinflow);
//        service.setOutFlow(serviceoutflow);
//        service.setFaultFlow(servicefaultflow);
//        service.setClassLoader(Thread.currentThread().getContextClassLoader());
//
//
//        ClassLoader serviceClassLoader = Thread.currentThread().getContextClassLoader();
//        /**
//         * ****************************************************************************
//         * ****************************************************************************
//         * Adding service inflow detail
//         */
//        count = serviceMetaData.getInFlow().getHandlercount();
//        addFlowHandlers(serviceinflow, count, serviceMetaData.getInFlow(), serviceClassLoader);
//
//        /**
//         * ****************************************************************************
//         * ****************************************************************************
//         * Adding service outflow detail
//         */
//        count = serviceMetaData.getOutFlow().getHandlercount();
//        addFlowHandlers(serviceoutflow, count, serviceMetaData.getOutFlow(), serviceClassLoader);
//
//        /**
//         * ****************************************************************************
//         * ****************************************************************************
//         * Adding service fault detail
//         */
//        count = serviceMetaData.getFaultFlow().getHandlercount();
//        addFlowHandlers(servicefaultflow, count, serviceMetaData.getFaultFlow(), serviceClassLoader);
//
//        /**
//         * ****************************************************************************
//         * ****************************************************************************
//         * Adding service parameters
//         */
//        count = serviceMetaData.getParametercount();
//        for (int j = 0; j < count; j++) {
//            ParameterMetaData paraMD = serviceMetaData.getParameter(j);
//            Parameter parameter = new ParameterImpl(paraMD.getName(), paraMD.getElement());
//            service.addParameter(parameter);
//        }
//
//        /**
//         * ****************************************************************************
//         * ****************************************************************************
//         * setting  service provider
//         */
//        service.setProvider(new SimpleJavaProvider());
//
//        OperationMetaData oprationmd = serviceMetaData.getOperation();
//        ModuleMetaData modulemd = oprationmd.getModule();
//
//        /**
//         * adding parametrs to module
//         */
//        AxisModule module = new AxisModule(new QName(modulemd.getRef()));
//        service.addModule(module.getName());
//        count = modulemd.getParameterCount();
//
//        for (int j = 0; j < count; j++) {
//            ParameterMetaData paraMD = modulemd.getParameter(j);
//            Parameter parameter = new ParameterImpl(paraMD.getName(), paraMD.getElement());
//            module.addParameter(parameter);
//        }
//
//        FlowImpl operationinflow = new FlowImpl();
//        count = oprationmd.getInFlow().getHandlercount();
//        addFlowHandlers(operationinflow, count, oprationmd.getInFlow(), serviceClassLoader);
//
//        FlowImpl operationutflow = new FlowImpl();
//        count = oprationmd.getOutFlow().getHandlercount();
//        addFlowHandlers(operationutflow, count, oprationmd.getOutFlow(), serviceClassLoader);
//
//        FlowImpl operationfaultflow = new FlowImpl();
//        count = oprationmd.getFaultFlow().getHandlercount();
//        addFlowHandlers(operationfaultflow, count, oprationmd.getFaultFlow(), serviceClassLoader);
//
//
//        QName opname = new QName(oprationmd.getName());
//        AxisOperation operation = new SimpleAxisOperationImpl(opname);
//
//        service.addOperation(operation);
//
//
//        ExecutionChain inchain = new ExecutionChain();
//        inchain.addPhase(new Phase(Constants.PHASE_TRANSPORT));
//        inchain.addPhase(new Phase(Constants.PHASE_GLOBAL));
//
//        /**
//         * todo in this implematation all the handers in the servcie
//         * i have asume as one phase bt that is not the case
//         * I have to modify that getting all the pahse and
//         * accooding tp that i have to create phases
//         */
//        Phase inservicephase = new Phase(Constants.PHASE_SERVICE);
//        HandlerMetaData[] handlerMetaDatas = serviceMetaData.getFlowHandlers(INFLOWST);
//        for (int i = 0; i < handlerMetaDatas.length; i++) {
//            HandlerMetaData handlerMetaData = handlerMetaDatas[i];
//            Handler handler = castHandlerMetaData(handlerMetaData, serviceClassLoader);
//            //TODO
//            //handler.setName(new QName(handlerMetaData.getName()));
//            inservicephase.addHandler(handler);
//        }
//        inchain.addPhase(inservicephase);
//
//
//        ExecutionChain outchain = new ExecutionChain();
//        outchain.addPhase(new Phase(Constants.PHASE_TRANSPORT));
//        outchain.addPhase(new Phase(Constants.PHASE_GLOBAL));
//
//        Phase outservicephase = new Phase(Constants.PHASE_SERVICE);
//        handlerMetaDatas = serviceMetaData.getFlowHandlers(OUTFLOWST);
//        for (int i = 0; i < handlerMetaDatas.length; i++) {
//            HandlerMetaData handlerMetaData = handlerMetaDatas[i];
//            Handler handler = castHandlerMetaData(handlerMetaData, serviceClassLoader);
//            handler.setName(new QName(handlerMetaData.getName()));
//            outservicephase.addHandler(handler);
//        }
//        outchain.addPhase(outservicephase);
//
//
//        ExecutionChain faultchain = new ExecutionChain();
//        faultchain.addPhase(new Phase(Constants.PHASE_TRANSPORT));
//        faultchain.addPhase(new Phase(Constants.PHASE_GLOBAL));
//
//        Phase faultservicephase = new Phase(Constants.PHASE_SERVICE);
//        handlerMetaDatas = serviceMetaData.getFlowHandlers(FAILTFLOWST);
//        for (int i = 0; i < handlerMetaDatas.length; i++) {
//            HandlerMetaData handlerMetaData = handlerMetaDatas[i];
//            Handler handler = castHandlerMetaData(handlerMetaData, serviceClassLoader);
//            handler.setName(new QName(handlerMetaData.getName()));
//            faultservicephase.addHandler(handler);
//        }
//        outchain.addPhase(faultservicephase);
//
//        service.setExecutableInChain(inchain);
//        service.setExecutableOutChain(outchain);
//        service.setExecutableFaultChain(faultchain);
//
//        engineRegistry.addService(service);
    }


    /**
     * this method used to add handlers to given Flow
     *
     * @param flow
     * @param count
     */
    private void addFlowHandlers(Flow flow, int count, FlowMetaData flowmetadata, ClassLoader parent) throws AxisFault {
//        for (int j = 0; j < count; j++) {
//            //todo handle exception in properway
//            HandlerMetaData handlermd = flowmetadata.getHandler(j);
//            Class handlerClass = null;
//            Handler handler;
//            handlerClass = getHandlerClass(handlermd.getClassName(), currentFileItem.getFile(), parent);
//            try {
//                handler = (Handler) handlerClass.newInstance();
//            } catch (InstantiationException e) {
//                throw new AxisFault(e.getMessage());
//            } catch (IllegalAccessException e) {
//                throw new AxisFault(e.getMessage());
//            }
//            handler.setName(new QName(handlermd.getName()));
//            int paracount = handlermd.getParacount();
//            for (int k = 0; k < paracount; k++) {
//                ParameterMetaData paraMD = handlermd.getParameter(k);
//                //todo check with srinath whether this is correct
////FIXME 
////                Parameter parameter = new ParameterImpl(paraMD.getName(), paraMD.getElement());
////                handler.addParameter(parameter);
//            }
//            flow.addHandler(handler);
//        }
    }


    public Class getHandlerClass(String className, File file, ClassLoader parent) throws AxisFault {
        Class handlerClass = null;
        if (file != null) {
            URL[] urlsToLoadFrom = new URL[0];
            try {
                if (!file.exists()) {
                    throw new RuntimeException("file not found !!!!!!!!!!!!!!!");
                }
                urlsToLoadFrom = new URL[]{file.toURL()};
            } catch (MalformedURLException e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            }
            URLClassLoader loader1 = new URLClassLoader(urlsToLoadFrom, parent);

            try {
                handlerClass = Class.forName(className, true, loader1);
            } catch (ClassNotFoundException e) {
                throw new AxisFault(e.getMessage());
            }
        }
        try {
            Handler handler = (Handler) handlerClass.newInstance();
            MessageContext msgContext = null;
            try {
                handler.invoke(msgContext);
            } catch (AxisFault axisFault) {
                throw new AxisFault(axisFault.getMessage());
            }
        } catch (InstantiationException e) {
            throw new AxisFault(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new AxisFault(e.getMessage());
        }
        return handlerClass;
    }


    private AxisModule castModuleMetaData(ModuleMetaData moduelmetada) throws AxisFault {
        ModuleMetaData modulemd = moduelmetada;

        /**
         * adding parametrs to module
         */
        AxisModule module = new AxisModule(new QName(modulemd.getName()));
        int count = modulemd.getParameterCount();

        //todo change the classloder
        ClassLoader moduleclassLoder = Thread.currentThread().getContextClassLoader();

        for (int j = 0; j < count; j++) {
            ParameterMetaData paraMD = modulemd.getParameter(j);
            Parameter parameter = new ParameterImpl(paraMD.getName(), paraMD.getElement());
            module.addParameter(parameter);
        }

        FlowImpl moduleinflow = new FlowImpl();
        count = modulemd.getInFlow().getHandlercount();
        addFlowHandlers(moduleinflow, count, modulemd.getInFlow(), moduleclassLoder);

        FlowImpl moduleoutflow = new FlowImpl();
        count = modulemd.getOutFlow().getHandlercount();
        addFlowHandlers(moduleoutflow, count, modulemd.getOutFlow(), moduleclassLoder);

        FlowImpl modulefaultflow = new FlowImpl();
        count = modulemd.getFaultFlow().getHandlercount();
        addFlowHandlers(modulefaultflow, count, modulemd.getFaultFlow(), moduleclassLoder);
        return module;
    }


    /**
     * this method used to cast Hander Metta data into hander
     *
     * @param handlerMetaData
     * @param serviceClassLoader
     * @return
     * @throws AxisFault
     */
    private Handler castHandlerMetaData(HandlerMetaData handlerMetaData, ClassLoader serviceClassLoader) throws AxisFault {
        Class handlerClass = null;
        Handler handler;
        // handlerClass = Class.forName(handlerMetaData.getClassName(), true, serviceClassLoader);
        handlerClass = getHandlerClass(handlerMetaData.getClassName(), currentFileItem.getFile(), serviceClassLoader);//Class.forName(handlerMetaData.getClassName(), true, serviceClassLoader);
        try {
            handler = (Handler) handlerClass.newInstance();
        } catch (InstantiationException e) {
            throw new AxisFault();
        } catch (IllegalAccessException e) {
            throw new AxisFault();
        }
        return handler;
    }


    /**
     * @param file
     */
    public void addtowsToDeploy(HDFileItem file) {
        wsToDeploy.add(file);
    }

    /**
     * @param file
     */
    public void addtowstoUnDeploy(WSInfo file) {
        wsToUnDeploy.add(file);
    }

    public void doDeploye() {
        //todo complete this
        if (wsToDeploy.size() > 0) {
            for (int i = 0; i < wsToDeploy.size(); i++) {
                currentFileItem = (HDFileItem) wsToDeploy.elementAt(i);
                int type = currentFileItem.getType();
                UnZipJAR unZipJAR = new UnZipJAR();
                switch (type) {
                    case SERVICE:
                        {
                            ServiceMetaData service = new ServiceMetaData();
                            service = unZipJAR.unzipService(currentFileItem.getAbsolutePath(), this);
                            try {
                                if (service != null) {
                                    addService(service);
                                    log.info("Deployement WS Name  " + currentFileItem.getName());
                                }
                            } catch (PhaseException e) {
                                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                            } catch (AxisFault axisFault) {
                                axisFault.printStackTrace();  //To change body of catch statement use Options | File Templates.
                            }
                        }
                    case MODULE:
                        {
                            ModuleMetaData metaData = new ModuleMetaData();
                            metaData = unZipJAR.unzipModule(currentFileItem.getAbsolutePath(), this);
                            try {
                                if (metaData != null) {
                                    addModule(metaData);
                                    log.info("Moduel WS Name  " + currentFileItem.getName() + " modulename :" + metaData.getName());
                                }
                            } catch (AxisFault axisFault) {
                                axisFault.printStackTrace();  //To change body of catch statement use Options | File Templates.
                            }
                        }
                }
            }
        }
        wsToDeploy.removeAllElements();
    }

    public void doUnDeploye() {
        //todo complete this
        if (wsToUnDeploy.size() > 0) {
            for (int i = 0; i < wsToUnDeploy.size(); i++) {
                WSInfo wsInfo = (WSInfo) wsToUnDeploy.elementAt(i);
                log.info("UnDeployement WS Name  " + wsInfo.getFilename());
            }

        }
        wsToUnDeploy.removeAllElements();
    }


}