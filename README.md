# UrlBuilder
根据请求接口自动生成Builder类

## 应用场景
我们在写接口的时候,如果遇到业务较为复杂参数众多的时候,会写很多类似这种代码,比如下面这种:
#### 网络层
    Observable<Response<CompanyList>> companyList(
            @Query("keyword") String keyword,
            @Query("biz_type") String biz_type,
            @Query("group_id") String group_id,
            @Query("origin") String origin,
            @Query("country") String[] country,
            @Query("province") String province,
            @Query("city") String city,
            @Query("curPage") Integer page_no,
            @Query("pageSize") Integer page_size,
            @Query("pin") Integer pin,
            @Query("sort_field") String sort_field,
            @Query("sort_type") String sort_type,
            @Query("star[]") String[] star,
            @Query("status_id[]") String[] status_id,
            @Query("tags[]") String[] tags,
            @Query("archive_start_date") String archive_start_date,
            @Query("archive_end_date") String archive_end_date,
            @Query("follow_up_start_date") String follow_up_start_date,
            @Query("follow_up_end_date") String follow_up_end_date,
            @Query("order_start_date") String order_start_date,
            @Query("order_end_date") String order_end_date,
            @Query("compare_day") Integer compare_day,
            @Query("compare_day_op") Integer compare_day_op,
            @Query("user_id[]") String[] user_id,
            @Query("user_num[]") String[] user_num,
            @Query("category_ids[]") String[] category_ids,
            @Query("lead_field") String lead_field,
            @Query("customer_field") String customer_field,
            @Query("show_all") Integer show_all,
            @Query("will_public") Integer will_public
    );
#### 数据传输层      
    public Observable<CompanyList> companyList(AccountModel accountModel, String group_id, String keyword, Integer page_no, Integer page_size, Integer pin, String sort_field, String sort_type, String[] star,
                                               String[] status_id, Integer tag_match_mode, String[] tags, Integer ownerType, Integer last_owner, String[] userId, String[] user_num, String type, String origin,
                                               String orderStartDate, String orderEndDate, String startDate, String endDate, Integer compareDay, Integer compareDayOp) {
       return getHttpClient(accountModel)
              .companyList(group_id, keyword, page_no, page_size, pin, sort_field, sort_type, star, status_id, tag_match_mode, tags, ownerType,
                      last_owner, userId, user_num, type, origin, orderStartDate, orderEndDate, startDate, endDate, compareDay, compareDayOp)
              .retryWhen(retry)
#### 界面层
    salesRepository.companyList(groupId, null, pageNo + 1, pageSize, pin, sortField, sortType, star, statusId, null, tags, ownerType, lastOwner,
            null, userNum, type, origin, null, null, startDate, endDate, compareDay, compareDayOp,
            GlobalRepository.get(getActivity()).account().name, NetWorkUtils.isNetWorkConnect(getActivity()))

是不是看着很头大...然而我们公司项目里面很多类似的接口...
刚好最近有空就研究了一下Android Studio的插件开发,然后给出了一套比较简单的解决方案
废话不多说,看改造效果

#### 网络层
    Observable<Response<LeadBeanList>> leadList(
            @Query("keyword") String keyword,
            @Query("biz_type") String biz_type,
            @Query("group_id") String group_id,
            @Query("origin") String origin,
            @Query("country") String[] country,
            @Query("province") String province,
            @Query("city") String city,
            @Query("curPage") Integer page_no,
            @Query("pageSize") Integer page_size,
            @Query("pin") Integer pin,
            @Query("sort_field") String sort_field,
            @Query("sort_type") String sort_type,
            @Query("star[]") String[] star,
            @Query("status_id[]") String[] status_id,
            @Query("tags[]") String[] tags,
            @Query("archive_start_date") String archive_start_date,
            @Query("archive_end_date") String archive_end_date,
            @Query("follow_up_start_date") String follow_up_start_date,
            @Query("follow_up_end_date") String follow_up_end_date,
            @Query("order_start_date") String order_start_date,
            @Query("order_end_date") String order_end_date,
            @Query("compare_day") Integer compare_day,
            @Query("compare_day_op") Integer compare_day_op,
            @Query("user_id[]") String[] user_id,
            @Query("user_num[]") String[] user_num,
            @Query("category_ids[]") String[] category_ids,
            @Query("lead_field") String lead_field,
            @Query("customer_field") String customer_field,
            @Query("show_all") Integer show_all,
            @Query("will_public") Integer will_public
    );
#### 数据传输层
    public Observable<LeadBeanList> leadList(AccountModel accountModel, LeadParams leadParams) {
        return getHttpClient(accountModel)
                .leadList(leadParams.getKeyword(), leadParams.getBizType(), leadParams.getGroupId(), leadParams.getOrigin(), leadParams.getCountry(),
                        leadParams.getProvince(), leadParams.getCity(), leadParams.getCurPage(), leadParams.getPageSize(), leadParams.getPin(),
                        leadParams.getSortField(), leadParams.getSortType(), leadParams.getStar(), leadParams.getStatusId(), leadParams.getTags(),
                        leadParams.getArchive_start_date(), leadParams.getArchive_end_date(), leadParams.getFollow_up_start_date(), leadParams.getFollow_up_end_date(),
                        leadParams.getOrder_start_date(), leadParams.getOrder_end_date(), leadParams.getCompare_day(), leadParams.getCompare_day_op(),
                        leadParams.getUserId(), leadParams.getUserNum(), leadParams.getCategoryIds(), leadParams.getLeadField(), leadParams.getCustomerField(), leadParams.getShowAll(),
                        leadParams.getWillPublic())
#### 界面层
                leadParamsBuilder.apply {
                    setUserId(follower)
                    setCompareDay(compareDay)
                    setCompareDayOp(compareDayOp)
                    setStatusId(status)
                    setOrigin(origin)
                    setCountry(country)
                    setProvince(province)
                    setCity(city)
                    setArchiveStartDate(createStartTime)
                    setArchiveEndDate(createEndTime)
                    setFollowUpStartDate(firstFollowStartTime)
                    setFollowUpEndDate(firstFollowEndTime)
                    setOrderStartDate(recentFollowStartTime)
                    setOrderEndDate(recentFollowEndTime)
                }
                onFilterListener?.onFilter(leadParamsBuilder.build())
        account.value?.let {
            dispose = crmRepository.leadList(it, leadParams)
                    .retryWhen(reLoginTry())
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        leadList.postValue(Resource.success(it))
                    }, {
                        leadList.postValue(Resource.error(it))
                    }, {}, {
                        leadList.postValue(Resource.loading())
                    })
        }
这样看起来代码是不是清爽了很多,然而下一个问题来了,这么多参数,LeadParamsBuilder还是写起来很麻烦..
nonono,使用本项目的插件,LeadParamsBuilder可以轻松一键生成

## Usage
#### 1.安装UrlBuilder.jar
#### 2.复制你需要生成Builder的接口,格式如下
    Observable<Response> saveContact(
            @Field("contact_id") String contact_id,
            @Field("group_id") String group_id,
            @Field("mail") String mail,
            @Field("nickname") String nickname,
            @Field("tel_area_code") String tel_area_code,
            @Field("tel") String tel,
            @Field("remark") String remark
    );
#### 3.新建domain类
    public class Pojo {
    }
    在{}中右键 -> Generate -> UrlBuilder -> 粘贴你复制的接口 -> Ok
#### 4.接下来就是见证奇迹的时刻
        public class Pojo {

          String userId;
          String taskId;

          class Builder {

          Pojo pojo = new Pojo();

          public Builder setUserId(String userId){
            pojo.userId = userId;
            return this; 
          }

          public Builder setTaskId(String taskId){
            pojo.taskId = taskId;
            return this; 
          }

          public Pojo build(){ 
            return pojo;
          }

          public void reset(){ 
            pojo = null;
            pojo = new Pojo();
          }
          }
        }
## Issue
目前暂时只支持文中贴出的接口格式,后续会支持更多
