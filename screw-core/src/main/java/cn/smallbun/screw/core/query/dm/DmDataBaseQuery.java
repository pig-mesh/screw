/*
 * screw-core - 简洁好用的数据库表结构文档生成工具
 * Copyright © 2020 SanLi (qinggang.zuo@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cn.smallbun.screw.core.query.dm;

import cn.smallbun.screw.core.exception.QueryException;
import cn.smallbun.screw.core.mapping.Mapping;
import cn.smallbun.screw.core.metadata.Column;
import cn.smallbun.screw.core.metadata.Database;
import cn.smallbun.screw.core.metadata.PrimaryKey;
import cn.smallbun.screw.core.query.AbstractDatabaseQuery;
import cn.smallbun.screw.core.query.dm.model.DmColumnModel;
import cn.smallbun.screw.core.query.dm.model.DmDatabaseModel;
import cn.smallbun.screw.core.query.dm.model.DmPrimaryKeyModel;
import cn.smallbun.screw.core.query.dm.model.DmTableModel;
import cn.smallbun.screw.core.util.Assert;
import cn.smallbun.screw.core.util.CollectionUtils;
import cn.smallbun.screw.core.util.ExceptionUtils;
import cn.smallbun.screw.core.util.JdbcUtils;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static cn.smallbun.screw.core.constant.DefaultConstants.PERCENT_SIGN;

/**
 * 达梦数据库查询
 *
 * @author SanLi
 * Created by jincy@neusoft.com + qinggang.zuo@gmail.com / 2689170096@qq.com on 2020/9/17 17:40
 */
@SuppressWarnings("serial")
public class DmDataBaseQuery extends AbstractDatabaseQuery {
    private final ConcurrentMap<String, List<DmTableModel>> tablesMap = new ConcurrentHashMap<>();

    private static final String DM_QUERY_TABLE_SQL =
            "select ut.table_name TABLE_NAME, utc.comments COMMENTS " +
                    "from user_tables ut " +
                    "left join USER_TAB_COMMENTS utc on ut.table_name = utc.table_name";

    private static final String DM_QUERY_COLUMNS_SQL =
            "SELECT\n" +
                    "    ut.table_name AS TABLE_NAME,\n" +
                    "    uc.column_name AS COLUMN_NAME,\n" +
                    "    CASE uc.data_type\n" +
                    "        WHEN 'CLOB' THEN\n" +
                    "            uc.data_type\n" +
                    "        WHEN 'BLOB' THEN\n" +
                    "            uc.data_type\n" +
                    "        ELSE\n" +
                    "            CONCAT(CONCAT(CONCAT(uc.data_type, '('), uc.data_length), ')')\n" +
                    "    END AS COLUMN_TYPE,\n" +
                    "    uc.data_length AS COLUMN_LENGTH,\n" +
                    "    uc.DATA_PRECISION AS DATA_PRECISION,\n" +
                    "    uc.DATA_SCALE AS DECIMAL_DIGITS,\n" +
                    "    CASE uc.NULLABLE\n" +
                    "        WHEN 'Y' THEN\n" +
                    "            '1'\n" +
                    "        ELSE\n" +
                    "            '0'\n" +
                    "    END AS NULLABLE,\n" +
                    "    uc.DATA_DEFAULT AS COLUMN_DEF,\n" +
                    "    ucc.comments AS REMARKS\n" +
                    "FROM\n" +
                    "    all_tables ut\n" +
                    "    LEFT JOIN all_tab_comments utc ON ut.table_name = utc.table_name AND ut.owner = utc.owner\n" +
                    "    LEFT JOIN all_tab_columns uc ON ut.owner = uc.owner AND ut.table_name = uc.table_name\n" +
                    "    LEFT JOIN all_col_comments ucc ON uc.owner = ucc.owner AND uc.table_name = ucc.table_name AND uc.column_name = ucc.column_name\n" +
                    "WHERE 1 = 1 ";

    private static final String DM_QUERY_PK_SQL =
            "SELECT C.OWNER AS TABLE_SCHEM, C.TABLE_NAME, C.COLUMN_NAME, C.POSITION AS KEY_SEQ, C.CONSTRAINT_NAME AS PK_NAME " +
                    "FROM ALL_CONS_COLUMNS C, ALL_CONSTRAINTS K " +
                    "WHERE K.CONSTRAINT_TYPE = 'P' AND K.OWNER = '%s' AND K.CONSTRAINT_NAME = C.CONSTRAINT_NAME " +
                    "AND K.TABLE_NAME = C.TABLE_NAME AND K.OWNER = C.OWNER";

    /**
     * 构造函数
     *
     * @param dataSource {@link DataSource}
     */
    public DmDataBaseQuery(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * 获取数据库
     *
     * @return {@link Database} 数据库信息
     */
    @Override
    public Database getDataBase() throws QueryException {
        DmDatabaseModel model = new DmDatabaseModel();
        //当前数据库名称
        model.setDatabase(getSchema());
        return model;
    }

    /**
     * 获取达梦数据库的schema
     *
     * @return {@link String} 达梦数据库的schema信息
     */
    @Override
    @SneakyThrows
    public String getSchema() throws QueryException {
        return getDataSource().getConnection().getSchema();
    }


    /**
     * 获取达梦数据库的schema
     *
     * @return {@link String} 达梦数据库的schema信息
     */
    public String getSchemaBak() throws QueryException {
        try {
            String schema = null;
            ResultSet rs = getMetaData().getSchemas();
            while (rs.next()) {
                schema = rs.getString(1);
                break;
            }
            return schema;
        } catch (Exception e) {
            throw ExceptionUtils.mpe(e);
        }
    }

    /**
     * 获取表信息
     *
     * @return {@link List} 所有表信息
     */
    @Override
    public List<DmTableModel> getTables() throws QueryException {
        ResultSet resultSet = null;
        try {
            //查询
            resultSet = getMetaData().getTables(getSchema(), getSchema(), PERCENT_SIGN, new String[]{"TABLE"});
            //映射
            List<DmTableModel> list = Mapping.convertList(resultSet, DmTableModel.class);

            resultSet = prepareStatement(DM_QUERY_TABLE_SQL).executeQuery();
            List<DmTableModel> inquires = Mapping.convertList(resultSet, DmTableModel.class);

            //处理备注信息
            list.forEach((DmTableModel model) -> {
                //备注
                inquires.stream().filter(inquire -> model.getTableName().equals(inquire.getTableName())).forEachOrdered(inquire -> model.setRemarks(inquire.getRemarks()));
            });
            if (!list.isEmpty()) {
                tablesMap.put("AllTable", list);
            }
            return list;
        } catch (SQLException e) {
            throw ExceptionUtils.mpe(e);
        } finally {
            JdbcUtils.close(resultSet, this.connection);
        }
    }

    /**
     * 获取列信息
     *
     * @param table {@link String} 表名
     * @return {@link List} 表字段信息
     */
    @Override
    public List<DmColumnModel> getTableColumns(String table) throws QueryException {
        Assert.notEmpty(table, "Table name can not be empty!");
        ResultSet resultSet = null;
        List<DmColumnModel> resultList = new ArrayList<>();
        List<String> tableNames = new ArrayList<>();
        try {
            //从缓存中获取表对象
            List<DmTableModel> tables = tablesMap.get("AllTable");
            if (tables.isEmpty()) {
                tables = getTables();
            } else {
                for (DmTableModel dtm : tables) {
                    tableNames.add(dtm.getTableName());
                }
            }
            /*如果表为空，则直接返回*/
            if (tableNames.isEmpty()) {
                return null;
            }

            if (CollectionUtils.isEmpty(columnsCaching)) {
                //查询全部
                if (table.equals(PERCENT_SIGN)) {
                    String sql = String.format(DM_QUERY_COLUMNS_SQL.concat(" AND ut.owner = '%s'"), getSchema());
                    PreparedStatement statement = prepareStatement(sql);
                    resultSet = statement.executeQuery();
                } else {
                    //查询单表的列信息
                    String singleTableSql = DM_QUERY_COLUMNS_SQL.concat(" and ut.table_name='%s'");
                    resultSet = prepareStatement(String.format(singleTableSql, table)).executeQuery();
                }

                List<DmColumnModel> inquires = Mapping.convertList(resultSet, DmColumnModel.class);
                //这里利用lambda表达式将多行列信息按table name 进行归类，并放入缓存
                tableNames.forEach(name -> columnsCaching.put(name, inquires.stream().filter(i -> i.getTableName().equals(name)).collect(Collectors.toList())));
                resultList = inquires;
            }
            return resultList;
        } catch (SQLException e) {
            throw ExceptionUtils.mpe(e);
        } finally {
            JdbcUtils.close(resultSet, this.connection);
        }
    }

    /**
     * 获取所有列信息
     *
     * @return {@link List} 表字段信息
     * @throws QueryException QueryException
     */
    @Override
    public List<? extends Column> getTableColumns() throws QueryException {
        return getTableColumns(PERCENT_SIGN);
    }

    /**
     * 根据表名获取主键
     *
     * @param table {@link String}
     * @return {@link List}
     * @throws QueryException QueryException
     */
    @Override
    public List<? extends PrimaryKey> getPrimaryKeys(String table) throws QueryException {
        ResultSet resultSet = null;
        try {
            //查询
            resultSet = getMetaData().getPrimaryKeys(getSchema(), getSchema(), table);
            //映射
            return Mapping.convertListByColumnLabel(resultSet, DmPrimaryKeyModel.class);
        } catch (SQLException e) {
            throw ExceptionUtils.mpe(e);
        } finally {
            JdbcUtils.close(resultSet, this.connection);
        }
    }

    /**
     * 根据表名获取主键
     *
     * @return {@link List}
     * @throws QueryException QueryException
     */
    @Override
    public List<? extends PrimaryKey> getPrimaryKeys() throws QueryException {
        ResultSet resultSet = null;
        try {
            // 由于单条循环查询存在性能问题，所以这里通过自定义SQL查询数据库主键信息
            String sql = String.format(DM_QUERY_PK_SQL, getSchema());
            resultSet = prepareStatement(sql).executeQuery();
            return Mapping.convertListByColumnLabel(resultSet, DmPrimaryKeyModel.class);
        } catch (SQLException e) {
            throw new QueryException(e);
        } finally {
            JdbcUtils.close(resultSet);
        }
    }
}
