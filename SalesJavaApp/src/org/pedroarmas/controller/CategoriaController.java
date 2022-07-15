package org.pedroarmas.controller;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javax.swing.JOptionPane;
import org.pedroarmas.bean.Categoria;
import org.pedroarmas.db.Conexion;
import org.pedroarmas.system.Principal;

public class CategoriaController implements Initializable {

    private Principal escenarioPrincipal;

    private enum operaciones {
        NUEVO, GUARDAR, ELIMINAR, EDITAR, ACTUALIZAR, CANCELAR, NINGUNO
    };
    private operaciones tipoDeOperacion = operaciones.NINGUNO;

    private ObservableList<Categoria> listaCategoria;

    @FXML
    private Button btnNuevo;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnReporte;

    @FXML
    private TextField txtCodigoCategoria;
    @FXML
    private TextField txtNombreCategoria;
    @FXML
    private TextField txtDescripcionCategoria;

    @FXML
    private TableView tblCategorias;

    @FXML
    private TableColumn colCodigoCategoria;
    @FXML
    private TableColumn colNombreCategoria;
    @FXML
    private TableColumn colDescripcionCategoria;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarDatos();
        colCodigoCategoria.setCellValueFactory(new PropertyValueFactory<Categoria, Integer>("codigoCategoria"));
        colNombreCategoria.setCellValueFactory(new PropertyValueFactory<Categoria, String>("nombreCategoria"));
        colDescripcionCategoria.setCellValueFactory(new PropertyValueFactory<Categoria, String>("descripcionCategoria"));
    }

    public void cargarDatos() {
        tblCategorias.setItems(getCategoria());
    }

    public ObservableList<Categoria> getCategoria() {
        ArrayList<Categoria> lista = new ArrayList<Categoria>();
        try {
            PreparedStatement procedimiento = Conexion.getInstance().getConexion().prepareCall("{call sp_ListarCategorias}");
            ResultSet resultado = procedimiento.executeQuery();
            while (resultado.next()) {
                lista.add(new Categoria(resultado.getInt("codigoCategoria"),
                        resultado.getString("nombreCategoria"),
                        resultado.getString("descripcionCategoria")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return listaCategoria = FXCollections.observableArrayList(lista);
    }

    public void seleccionarElemento() {
        if (tblCategorias.getSelectionModel().getSelectedItem() != null) {
            txtCodigoCategoria.setText(String.valueOf(((Categoria) tblCategorias.getSelectionModel().getSelectedItem()).getCodigoCategoria()));
            txtNombreCategoria.setText(((Categoria) tblCategorias.getSelectionModel().getSelectedItem()).getNombreCategoria());
            txtDescripcionCategoria.setText(((Categoria) tblCategorias.getSelectionModel().getSelectedItem()).getDescripcionCategoria());
        }
    }

    public void nuevo() {
        switch (tipoDeOperacion) {
            case NINGUNO:
                activarControles();
                btnNuevo.setText("Guardar");
                btnEliminar.setText("Cancelar");
                btnEditar.setDisable(true);
                btnReporte.setDisable(true);
                tipoDeOperacion = operaciones.GUARDAR;
                break;

            case GUARDAR:
                guardar();
                desactivarControles();
                limpiarControles();
                btnNuevo.setText("Nuevo");
                btnEliminar.setText("Eliminar");
                btnEditar.setDisable(false);
                btnReporte.setDisable(false);
                tipoDeOperacion = operaciones.NINGUNO;
                cargarDatos();
                break;
        }
    }

    public void guardar() {
        Categoria registro = new Categoria();
        registro.setNombreCategoria(txtNombreCategoria.getText());
        registro.setDescripcionCategoria(txtDescripcionCategoria.getText());
        try {
            PreparedStatement procedimiento = Conexion.getInstance().getConexion().prepareCall("{call sp_AgregarCategoria(?,?)}");
            procedimiento.setString(1, registro.getNombreCategoria());
            procedimiento.setString(2, registro.getDescripcionCategoria());
            procedimiento.execute();
            listaCategoria.add(registro);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void eliminar() {
        switch (tipoDeOperacion) {
            case GUARDAR: {
                desactivarControles();
                limpiarControles();
                btnNuevo.setText("Nuevo");
                btnEliminar.setText("Eliminar");
                btnEditar.setDisable(false);
                btnReporte.setDisable(false);
                tipoDeOperacion = operaciones.NINGUNO;
            }
            break;

            default: {
                if (tblCategorias.getSelectionModel().getSelectedItem() != null) {
                    int respuesta = JOptionPane.showConfirmDialog(null, "¿Está seguro de eliminar el registro?", "Eliminar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (respuesta == JOptionPane.YES_OPTION) {
                        try {
                            PreparedStatement procedimiento = Conexion.getInstance().getConexion().prepareCall("{call sp_EliminarCategoria(?)}");
                            procedimiento.setInt(1, ((Categoria) tblCategorias.getSelectionModel().getSelectedItem()).getCodigoCategoria());
                            procedimiento.execute();
                            listaCategoria.remove(tblCategorias.getSelectionModel().getSelectedIndex());
                            limpiarControles();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Debe seleccionar un registro", "Atención", JOptionPane.WARNING_MESSAGE);
                }
            }
            break;
        }
    }

    public void editar() {
        switch (tipoDeOperacion) {
            case NINGUNO: {
                if (tblCategorias.getSelectionModel().getSelectedItem() != null) {
                    btnNuevo.setDisable(true);
                    btnEliminar.setDisable(true);
                    btnEditar.setText("Actualizar");
                    btnReporte.setText("Cancelar");
                    activarControles();
                    tipoDeOperacion = operaciones.ACTUALIZAR;
                } else {
                    JOptionPane.showMessageDialog(null, "Debe seleccionar un registro", "Atención", JOptionPane.WARNING_MESSAGE);
                }
            }
            break;

            case ACTUALIZAR: {
                actualizar();
                btnNuevo.setDisable(false);
                btnEliminar.setDisable(false);
                btnEditar.setText("Editar");
                btnReporte.setText("Reporte");
                limpiarControles();
                desactivarControles();
                cargarDatos();
                tipoDeOperacion = operaciones.NINGUNO;
            }
            break;
        }
    }

    public void actualizar() {
        try {
            PreparedStatement procedimiento = Conexion.getInstance().getConexion().prepareCall("{call sp_EditarCategoria(?,?,?)}");
            Categoria registro = (Categoria) tblCategorias.getSelectionModel().getSelectedItem();
            registro.setNombreCategoria(txtNombreCategoria.getText());
            registro.setDescripcionCategoria(txtDescripcionCategoria.getText());
            procedimiento.setInt(1, registro.getCodigoCategoria());
            procedimiento.setString(2, registro.getNombreCategoria());
            procedimiento.setString(3, registro.getDescripcionCategoria());
            procedimiento.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reporte() {
        switch (tipoDeOperacion) {
            case ACTUALIZAR: {
                desactivarControles();
                limpiarControles();
                btnEditar.setText("Editar");
                btnReporte.setText("Reporte");
                btnNuevo.setDisable(false);
                btnEliminar.setDisable(false);
                tipoDeOperacion = operaciones.NINGUNO;
            }
            break;
        }
    }

    public void desactivarControles() {
        txtCodigoCategoria.setEditable(false);
        txtNombreCategoria.setEditable(false);
        txtDescripcionCategoria.setEditable(false);
    }

    public void activarControles() {
        txtCodigoCategoria.setEditable(false);
        txtNombreCategoria.setEditable(true);
        txtDescripcionCategoria.setEditable(true);
    }

    public void limpiarControles() {
        txtCodigoCategoria.clear();
        txtNombreCategoria.clear();
        txtDescripcionCategoria.clear();
        tblCategorias.getSelectionModel().clearSelection();

    }

    public Principal getEscenarioPrincipal() {
        return escenarioPrincipal;
    }

    public void setEscenarioPrincipal(Principal escenarioPrincipal) {
        this.escenarioPrincipal = escenarioPrincipal;
    }

}