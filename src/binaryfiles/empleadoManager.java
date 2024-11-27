package binaryfiles;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Date;

public class empleadoManager {
    
    private RandomAccessFile rcods, remps;
    
    /*
        Formato Codigo.emp
        int code
        Formato Empleados.emp
        int code
        String name
        double salary
        long fecha Contratacion
        long fecha despido
    */
    
    public empleadoManager () {
        try {
            //1 - Asegurar que el folder company exista
            File mf = new File("company");
            mf.mkdir();
            //2 - Instanciar RAFs dentro de company
            rcods = new RandomAccessFile("company/codigos.emp", "rw");
            remps = new RandomAccessFile("company/empleado.emp", "rw");
            initCodes();
        } catch (IOException e) {
            System.out.println("Error"+e.getMessage());
        }
    }
    
    private void initCodes()throws IOException{
        if (rcods.length()==0) {
            //Puntero ->   0
            rcods.writeInt(1);
            //Puntero ->   4
        }
    }
    
    private int getCode() throws IOException{
        rcods.seek(0);
        //Puntero     ->       0
        int code=rcods.readInt();
        //Puntero     ->       4
        rcods.seek(0);
        rcods.writeInt(code+1);
        return code;
    }
    
    public void addEmployee(String name, double salary) throws IOException{
        //Asegurar que el puntero este en el final del archivo
        remps.seek(remps.length());
        int code = getCode();
        //P -> 0
        remps.writeInt(code);
        //P -> 4
        remps.writeUTF(name);//Ana 8
        //p -> 12
        remps.writeDouble(salary);
        //P -> 20
        remps.writeLong(Calendar.getInstance().getTimeInMillis());
        //P -> 28
        remps.writeLong(0);
        //P -> 36 EOF
        //Asegurar crear folder y archivos individuales
        createEmployeeFolders(code);
    }
    
    private String employeeFolder(int code){
        return "company/empleado"+code;
    }
    
    private void createEmployeeFolders(int code) throws IOException{
        //Crear folder empleado+code
        File empDIR = new File(employeeFolder(code));
        empDIR.mkdir();
    }
    
    private RandomAccessFile salesFileFor(int code)throws IOException{
        String DIRpadre = employeeFolder(code);
        int yearActual = Calendar.getInstance().get(Calendar.YEAR);
        String path = DIRpadre+"/ventas"+yearActual+".emp";
        return new RandomAccessFile(path, "rw");
    }
    
    private void createYearSalesFileFor(int code)throws IOException{
        
        RandomAccessFile ryear = salesFileFor(code);
        if(ryear.length()==0){
            for (int mes = 0; mes < 12; mes++) {
                ryear.writeDouble(0);
                ryear.writeBoolean(false);
                
            }
            
        }
    }
    //Code - Name - Salary - Fecha Con.
    public void employeeList()throws IOException{
        remps.seek(0);
        //P-->  36 < 36 False
        while(remps.getFilePointer ()<= remps.length()){
            //P->0
            int code=remps.readInt();
            //P->4
            String name=remps.readUTF();
            //P->12
            double salary=remps.readDouble();
            //P-> 20
            Date dateH= new Date(remps.readLong());
            //P->28
            if(remps.readLong()==0){
                System.out.println("Codigo: "+code+"Nombre: "+name+
                        "Salario: Lps."+salary+"Contratado: "+dateH);  
            }
            //P->36
            
        }
        
    }
    
    private boolean isEmployeeActive(int code)throws IOException{
        remps.seek(0);
        while(remps.getFilePointer()<remps.length()){
            int codigo=remps.readInt();
            long pos=remps.getFilePointer();
            remps.readUTF();
            remps.skipBytes(16);
            if (remps.readLong()==0&&codigo==code) {
                remps.seek(pos);
                return true;
            }
        }
        return false;
    }
    public boolean fireEmployee(int code)throws IOException{
        if (isEmployeeActive(code)) {
            String name = remps.readUTF();
            remps.skipBytes(16);
            remps.writeLong(new Date().getTime());
            System.out.println("Despidiendo a: "+name);
            return true;
        }
        return false;
    }
    
    public void addSaleToEmployee(int code, double monto)throws IOException{
        if (isEmployeeActive(code)) {
            RandomAccessFile salesFile = salesFileFor(code);
            Calendar calendar = Calendar.getInstance();
            int mesActual = calendar.get(Calendar.MONTH);
            
            salesFile.seek(mesActual * 9);
            double ventasActuales = salesFile.readDouble();
            boolean mesPagado = salesFile.readBoolean();
            
            if (!mesPagado) {
                salesFile.seek(mesActual * 9);
                salesFile.writeDouble(ventasActuales + monto);
                System.out.println("Venta agregada a empleado: "+code+": Lps."+monto);
            } else {
                System.out.println("Error: El mes ya fue cerrado para este empleado.");
            }
        } else {
            System.out.println("Error: Empleado no activo o inexistente.");
        }
    }
    
    public void payEmployee(int code) throws IOException {
        
        if (isEmployeeActive(code)) {
            RandomAccessFile salesFile = salesFileFor(code);
            Calendar calendar = Calendar.getInstance();
            int mesActual = calendar.get(Calendar.MONTH);
            int yearActual = calendar.get(Calendar.YEAR);
            
            salesFile.seek(mesActual * 9);
            double ventasMes = salesFile.readDouble();
            boolean mesPagado = salesFile.readBoolean();
            
            if (!mesPagado) {
                remps.seek(0);
                while (remps.getFilePointer()<remps.length()) {
                    int empleadoCode = remps.readInt();
                    long posNombre = remps.getFilePointer();
                    String nombre = remps.readUTF();
                    double salario = remps.readDouble();
                    remps.skipBytes(16);
                    
                    if (empleadoCode == code) {
                        double comision = ventasMes * 0.10;
                        double sueldoBase = salario + comision;
                        double deduccion = sueldoBase * 0.035;
                        double sueldoNeto = sueldoBase - deduccion;
                        long fechaPago = new Date().getTime();
                        
                        RandomAccessFile recibosFile = new RandomAccessFile(employeeFolder(code) + "/recibos.emp", "rw");
                        recibosFile.seek(recibosFile.length());
                        recibosFile.writeLong(fechaPago);
                        recibosFile.writeDouble(comision);
                        recibosFile.writeDouble(sueldoBase);
                        recibosFile.writeDouble(deduccion);
                        recibosFile.writeDouble(sueldoNeto);
                        recibosFile.writeInt(yearActual);
                        recibosFile.writeInt(mesActual);
                        
                        salesFile.seek(mesActual * 9 + 8);
                        salesFile.writeBoolean(true);
                        
                        System.out.println("Pago realizado a "+nombre+": Lps."+sueldoNeto);
                        return;
                    }
                }
            } else {
                System.out.println("Error: Este mes ya se le ha pagado al empleado.");
            }
        } else {
            System.out.println("Error: Empleado no activo o inexsistente.");
        }
        
    }
    
    public void printEmployee(int code) throws IOException {
        remps.seek(0);
        while (remps.getFilePointer() < remps.length()) {
            int empleadoCode = remps.readInt();
            String nombre = remps.readUTF();
            double salario = remps.readDouble();
            long fechaContratacion = remps.readLong();
            long fechaDespido = remps.readLong();

            if (empleadoCode == code) {
                System.out.println("Código: " + empleadoCode);
                System.out.println("Nombre: " + nombre);
                System.out.println("Salario: Lps." + salario);
                System.out.println("Fecha de contratación: " + new Date(fechaContratacion));
                System.out.println("Estado: " + (fechaDespido == 0 ? "Activo" : "Despedido en " + new Date(fechaDespido)));

                RandomAccessFile salesFile = salesFileFor(code);
                double totalVentas = 0;
                System.out.println("Ventas anuales:");
                for (int mes = 0; mes < 12; mes++) {
                    salesFile.seek(mes * 9);
                    double ventasMes = salesFile.readDouble();
                    System.out.println("Mes " + (mes + 1) + ": Lps." + ventasMes);
                    totalVentas += ventasMes;
                }
                System.out.println("Total de ventas: Lps." + totalVentas);

                RandomAccessFile recibosFile = new RandomAccessFile(employeeFolder(code) + "/recibos.emp", "r");
                int totalRecibos = 0;
                while (recibosFile.getFilePointer() < recibosFile.length()) {
                    recibosFile.skipBytes(44); 
                    totalRecibos++;
                }
                System.out.println("Total de recibos históricos: " + totalRecibos);
                return;
            }
        }
        System.out.println("Error: Empleado no encontrado.");
    }
    
}