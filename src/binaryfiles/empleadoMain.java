package binaryfiles;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class empleadoMain {
    public static void main(String[] args) {
        Scanner lea = new Scanner(System.in).useDelimiter("\n");
        empleadoManager manager = new empleadoManager();
        int options = 0;
        
        while(options!=6){
            System.out.println("\n\n**** MENU ****\n");
            System.out.println("1 - Agregar Empleado");
            System.out.println("2 - Listar Empleado No despedidos");
            System.out.println("3 - Agregar Venta al Empleado");
            System.out.println("4 - Pagar Empleado");
            System.out.println("5 - Despedir a Empleado");
            System.out.println("6 - Salir");
            System.out.println("Escoja una opcion: ");
            try{
                options = lea.nextInt();

                switch (options) {
                    case 1:
                        System.out.print("Ingrese el nombre del empleado: ");
                        String name = lea.next();
                        System.out.print("Ingrese el salario del empleado: ");
                        double salary = lea.nextDouble();
                        manager.addEmployee(name, salary);
                        System.out.println("Empleado agregado exitosamente.");
                        break;

                    case 2:
                        System.out.println("\nEmpleados Activos:");
                        manager.employeeList();
                        break;

                    case 3:
                        System.out.print("Ingrese el código del empleado: ");
                        int code = lea.nextInt();
                        System.out.print("Ingrese el monto de la venta: ");
                        double monto = lea.nextDouble();
                        manager.addSaleToEmployee(code, monto);
                        break;

                    case 4:
                        System.out.print("Ingrese el código del empleado: ");
                        code = lea.nextInt();
                        manager.payEmployee(code);
                        break;

                    case 5: 
                        System.out.print("Ingrese el código del empleado: ");
                        code = lea.nextInt();
                        if (manager.fireEmployee(code)) {
                            System.out.println("Empleado despedido exitosamente.");
                        } else {
                            System.out.println("Error: No se pudo despedir al empleado. Verifique el código.");
                        }
                        break;

                    case 6: 
                        System.out.println("Saliendo del sistema...");
                        break;

                    default:
                        System.out.println("Opción no válida. Por favor, intente de nuevo.");
                        break;
                }
                
            } catch (InputMismatchException e) {
                System.out.println("Ingresar una opcion valida!");
                lea.next();
            } catch(IOException e){
                System.out.println("Error de archivo: "+e.getMessage());
            }catch (NullPointerException e) {
                System.out.println("Por favor, Sleccionar primero la opcion 1");
            }
        }
        lea.close();
    }
}
