from manimlib.imports import *
from abc import ABC, abstractmethod


class Main(Scene):
    def construct(self):
        # Building code visualisation pane
        code_block = Code_block(["let y = new Stack<number>;","y.push(2);","y.push(3);","y.pop();"])
        code_text = code_block.build()
        self.place_at(code_text, -1, 0)
        self.play(FadeIn(code_text))
        # Constructing current line pointer
        pointer = ArrowTip(color=YELLOW).scale(0.7).flip(TOP)
        # Moves the current line pointer to line 1
        self.move_arrow_to_line(1, pointer, code_block)
        # Constructing new Stack<number> "y"
        stack = Stack([5, 4, 0], [7, 4, 0], [5, -4, 0], [7, -4, 0], DOWN)
        self.play(stack.create_init("y"))
        self.move_arrow_to_line(2, pointer, code_block)
        # Constructs a new Rectangle_block with value 2.0
        rectangle = Rectangle_block("2.0", stack)
        [self.play(*animation) for animation in stack.push(rectangle)]
        stack.add(rectangle.all)
        self.move_arrow_to_line(3, pointer, code_block)
        # Constructs a new Rectangle_block with value 3.0
        rectangle1 = Rectangle_block("3.0", stack)
        [self.play(*animation) for animation in stack.push(rectangle1)]
        stack.add(rectangle1.all)
        self.move_arrow_to_line(4, pointer, code_block)
        [self.play(*animation) for animation in stack.pop(rectangle1, fade_out=True)]
    def place_at(self, group, x, y):
        group.to_edge(np.array([x, y, 0]))
    def move_relative_to_edge(self, group, x, y):
        self.play(ApplyMethod(group.to_edge, np.array([x, y, 0])))
    def move_relative_to_obj(self, group, target, x, y):
        self.play(ApplyMethod(group.next_to, target, np.array([x, y, 0])))
    def place_relative_to_obj(self, group, target, x, y):
        group.next_to(target, np.array([x, y, 0]))
    def move_arrow_to_line(self, line_number, pointer, code_block):
        line_object = code_block.get_line_at(line_number)
        self.play(FadeIn(pointer.next_to(line_object, LEFT, MED_SMALL_BUFF)))
# Object representing the visualised code on the left hand side of the screen
class Code_block:
    def __init__(self, code, text_color=WHITE, text_weight=NORMAL, font="Times New Roman"):
        group = VGroup()
        for c in code:
            group.add(Text(c, color=text_color, weight=text_weight, font=font))
        group.set_width(5)
        self.all = group
    def build(self):
        return self.all.arrange(DOWN, aligned_edge=LEFT)
    def get_line_at(self, line_number):
        return self.all[line_number - 1]
class DataStructure(ABC):
    def __init__(self, ul, ur, ll, lr, aligned_edge, color=WHITE, text_color=WHITE, text_weight=NORMAL, font="Times New Roman"):
        self.ul = ul
        # self.ur = ur
        # self.ll = ll
        self.lr = lr
        self.max_width = self.lr[0] - self.ul[0]
        self.width_center = self.ul[0] + self.max_width / 2
        self.max_height = self.ul[1] - self.lr[1]
        self.aligned_edge = aligned_edge
        self.color = color
        self.text_color = text_color
        self.text_weight = text_weight
        self.font = font
        self.all = VGroup()
    def shrink(self, new_width, new_height):
        scale_factor = min((self.max_width - 2 * MED_SMALL_BUFF) / new_width, self.max_height / new_height)
        if scale_factor != 1:
            return ApplyMethod(self.all.scale, scale_factor, {"about_edge": self.aligned_edge}), scale_factor
        return 0, 1
    def will_cross_boundary(self, object_dim, boundary_name):
        boundary_options = {"TOP": self.will_cross_top_boundary, "RIGHT": self.will_cross_right_boundary}
        return boundary_options[boundary_name](object_dim)
    def will_cross_top_boundary(self, object_height):
        frame_top_y = self.ul[1]
        group_top_y = self.all.get_top()[1]
        return group_top_y + object_height > frame_top_y
    def will_cross_right_boundary(self, object_width):
        frame_right_x = self.lr[0]
        group_right_x = self.all.get_right()[0]
        return group_right_x + object_width > frame_right_x
    def has_crossed_top_boundary(self):
        frame_top_y = self.ul[1]
        group_top_y = self.all.get_top()[1]
        return group_top_y > frame_top_y
    def add(self, obj):
        self.all.add(obj)
    @abstractmethod
    def create_init(self, ident):
        pass
    @abstractmethod
    def shrink_if_cross_border(self, obj):
        pass
class Stack(DataStructure, ABC):
    def __init__(self, ul, ur, ll, lr, aligned_edge, color=WHITE, text_color=WHITE, text_weight=NORMAL,font="Times New Roman"):
        super().__init__(ul, ur, ll, lr, aligned_edge, color, text_color, text_weight, font)
        self.empty = None
    def create_init(self, text):
        empty = Init_structure(text, 0, self.max_width - 2 * MED_SMALL_BUFF, color=self.color, text_color=self.text_color)
        self.empty = empty.all
        empty.all.move_to(np.array([self.width_center, self.lr[1], 0]), aligned_edge=self.aligned_edge)
        self.all.add(empty.all)
        return ShowCreation(empty.all)
    def push(self, obj):
        animations = []
        obj.all.move_to(np.array([self.width_center, self.ul[1] - 0.1, 0]), UP)
        shrink, scale_factor = self.shrink_if_cross_border(obj.all)
        if shrink:
            animations.append([shrink])
        target_width = self.all.get_width() * (scale_factor if scale_factor else 1)
        obj.all.scale(target_width / obj.all.get_width())
        animations.append([FadeIn(obj.all)])
        animations.append([ApplyMethod(obj.all.next_to, self.all, np.array([0, 0.25, 0]))])
        return animations
    def pop(self, obj, fade_out=True):
        self.all.remove(obj.all)
        animation = [[ApplyMethod(obj.all.move_to, np.array([self.width_center, self.ul[1] - 0.1, 0]), UP)]]
        if fade_out:
            animation.append([FadeOut(obj.all)])
            enlarge, scale_factor = self.shrink(new_width=self.all.get_width(), new_height=self.all.get_height() + 0.25)
            if enlarge:
                animation.append([enlarge])
        return animation
    def shrink_if_cross_border(self, new_obj):
        height = new_obj.get_height()
        if self.will_cross_boundary(height, "TOP"):
            return self.shrink(new_width=self.all.get_width(), new_height=self.all.get_height() + height + 0.4)
        return 0, 1
    def push_existing(self, obj):
        animation = [[ApplyMethod(obj.all.move_to, np.array([self.width_center, self.ul[1] - 0.1, 0]), UP)]]
        enlarge, scale_factor = obj.owner.shrink(new_width=obj.owner.all.get_width(), new_height=obj.owner.all.get_height() + 0.25)
        sim_list = list()
        if enlarge:
            sim_list.append(enlarge)
        scale_factor = self.all.get_width() / obj.all.get_width()
        if scale_factor != 1:
            sim_list.append(ApplyMethod(obj.all.scale, scale_factor, {"about_edge": UP}))
        if len(sim_list) != 0:
            animation.append(sim_list)
        animation.append([ApplyMethod(obj.all.next_to, self.all, np.array([0, 0.25, 0]))])
        return animation
# Object representing a stack instantiation.
class Init_structure:
    def __init__(self, text, angle, length=1.5, color=WHITE, text_color=WHITE, text_weight=NORMAL, font="Times New Roman"):
        self.shape = Line(color=color)
        self.shape.set_length(length)
        self.shape.set_angle(angle)
        self.text = Text(text, color=text_color, weight=text_weight, font=font)
        self.text.next_to(self.shape, DOWN, SMALL_BUFF)
        self.all = VGroup(self.text, self.shape)
class Rectangle_block:
    def __init__(self, text, target=None, height=0.75, width=1.5, color=BLUE, text_color=WHITE, text_weight=NORMAL, font="Times New Roman"):
        self.text = Text(text, color=text_color, weight=text_weight, font=font)
        self.shape = Rectangle(height=height, width=width, color=color)
        self.text.scale(self.shape.get_height() * 0.75 / self.text.get_height())
        self.all = VGroup(self.text, self.shape)
        if target:
            self.owner = target
            self.all.scale(max(target.empty.submobjects[1].get_height() / self.shape.get_height(), target.empty.get_width() / self.shape.get_width()))