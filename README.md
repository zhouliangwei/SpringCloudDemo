# SpringCloudDemo
有关springcloud的小demo
有待继续完善

以前的服务器就好像，一个会语数外全能的老师，为学生提供服务，这个老师生病了，那全校停课。
微服务就像一个学校，有了数学教研组，语文教研组，外语教研组，每个教研组有一群老师具体负责某科的教学，缺了谁，学校都照样运转。
1.Eureka（服务的发现和注册） - （学校老师们的名单）
（1）Eureka-server
	创建一个服务端，以供其他的客户端来注册。
（2）Eureka-client
	连接到Eureka-server，就好像老师到学校报名了，然后实现自己的价值。
	
	实现方法:
		（1）server端在配置文件中配置端口、服务名等等
		（2）client端，在配置文件中，注册到server端口，在启动类加上 @EnableClient注解就注册到服务端了。

2.ribbon + restTemplate（负载均衡）[注解直接是在service层类上的]
	一个学校不可能只有一个数学老师，如果每个班的试卷都让一个老师批改，就会让这个老师很累，很容易垮掉。
	所以ribbon就改变了这个状态，在收到批改试卷的要求时候，这一批给老师1，另一批给老师2.
	从而达到了，负载均衡的效果。即，发送同一个请求时，负载到各个服务上。
	
	实现方法：
		（1）在配置文件中制定端口注册到服务端。
		（2）在启动类中添加 @EnableDiscoveryClient，并向bean池中添加 resttemplate 类（在resttemplate的bean上添加一个loadblanced注解，即添加了一个拦截器，spring可以处理resttemplate的bean，给你分配端口）
		（3）在service层，注入（autowired）resttemplate类，然后创建对象，调用RestTemplate的方法，例：return restTemplate.getForObject("http://EUREKA-SERVICE/hello",String.class);
		（4）在controller层调用该对象，即可以负载均衡。（EUREKA-SERVICE时client的服务名，可以开启多个端口，通过负载均衡分配到每个端口）

	注：RestTemplate 是从 Spring3.0 开始支持的一个 HTTP 请求工具，它提供了常见的REST请求方案的模版，
	例如 GET 请求、POST 请求、PUT 请求、DELETE 请求以及一些通用的请求执行方法 exchange 以及 execute。
	RestTemplate 继承自 InterceptingHttpAccessor 并且实现了 RestOperations 接口，
	其中 RestOperations 接口定义了基本的 RESTful 操作，这些操作在 RestTemplate 中都得到了实现。
	
	getForEntity、getForObject（这两个的差异主要体现在返回值的差异上， getForObject 的返回值就是服务提供者返回的数据，使用 getForObject 无法获取到响应头。）
	postForEntity、postForObject、postForLocation（postForObject 和 postForEntity 基本一致，就是返回类型不同而已，这里不再赘述。postForLocation 方法的返回值是一个 Uri 对象）
	put、delete（）
	https://blog.csdn.net/qwe86314/article/details/97281910

3.feign （负载均衡，封装了ribbon）[注解是在service层接口上]
	封装了ribbon，不需要使用RestTemplate去配合着负载均衡，在service接口层添加注释即可。
	
	实现方法：
		（1）在配置文件中制定端口注册到服务端
		（2）在启动类，添加两个注释 @EnableDiscoveryClient @EnableFeignClients
		（3）在service层，加去@FeignClient注解，然后该service层，就可以当做controller层来负载均衡其他客户端的方法了。
			例：@FeignClient(value = "EUREKA-SERVICE")
				@Repository //加上这个在controller层就不会有红叉（红叉是因为这个Bean是在程序启动的时候注入的，编译器感知不到，所以报错）
				public interface FeignService {
					@GetMapping("/hello")
					String hiFeign();
				}
			即是调用了 EUREKA-SERVICE的hello方法，接下来的负载均衡就是跟ribbon一样了。

4.hystrix
	那Hystrix熔断器呢，可以把它当成学校的志愿者，当一个教研组集体罢课后，学生找不到老师了，这些志愿者及时的告诉来访问的学生，
	相应的结果，异常信息等，免得大量的学生在学校等待，这些志愿者赶快把这些等待的学生梳理出去，学生一直在学校等待，
	那其他需要学生的学校，也会等待学生，最后造成大面积的学校瘫痪。这里学生我们看成一个个请求。
	可以配合ribbon和feign来使用。

	A.配合ribbon使用的实现方法：
		（1）在启动类中添加 @EnableHystrix 
		（2）在service层，添加@HystrixCommand(fallback="方法名")
		（3）在该sevice层添加上面的方法名，但出现错误的时候（延时或者直接切断），就会调用这个方法
		（4）关于hystrix的配置，可在配置文件中配置，可以在command下配置。
			例子：
				1）@HystrixCommand(fallbackMethod = "hiError",
						commandProperties = {
								@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000" )
						})
				2）hystrix:
					  command:
						default: #也可以针对多个服务
						  execution:
							isolation:
							  thread:
								timeoutInMilliseconds: 3000 # 设置hystrix的超时时间为3000ms
	B.配合feign使用的实现方法：
		（1）因为feign封装了hystrix，所以在@FeignClient注解后面，添加fallback即可。
			例：@FeignClient(value = "EUREKA-SERVICE",fallback = FeignServiceFallBack.class)
				FeignServiceFallBack.class中定义fallback的方法，FeignServiceFallBack.class是实现feign注解下的service接口的实现类。

5.zuul
	Zuul网关，就是学校的门卫，某些学生来学校找谁，它负责指引（路由），并且通过一些非常简单的配置，达到阻拦一些人进入（身份验证），
	或者控制想学数学的人只能去数学教研组，不能去核能教研组学怎么造原子弹（权限验证）。
	
	实现方法：
		（1）在启动类中添加 @EnableDiscoveryClient 、@EnableZuulProxy
		（2）在配置文件中制定端口注册到服务端
		（3）在配置文件中，配置分发路由。
		（4）例：以/api-a/ 开头的请求都转发给service-ribbon服务；以/api-b/开头的请求都转发给service-feign服务；
			zuul:
			  host:
				socket-timeout-millis: 60000
				connect-timeout-millis: 60000
			  routes:
				api-a:
				  path: /api-a/**
				  serviceId: service-ribbon
				api-b:
				  path: /api-b/**
				  serviceId: service-feign

6.springcloudconfg 待完善